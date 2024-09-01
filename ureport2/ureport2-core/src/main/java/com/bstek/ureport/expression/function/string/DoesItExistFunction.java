/*
 * All content copyright http://www.j2eefast.com, unless
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */
package com.bstek.ureport.expression.function.string;

import cn.hutool.core.util.StrUtil;
import com.bstek.ureport.Utils;
import com.bstek.ureport.build.BindData;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.expression.model.data.BindDataListExpressionData;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.expression.model.data.ObjectExpressionData;
import com.bstek.ureport.expression.model.data.ObjectListExpressionData;
import com.bstek.ureport.model.Cell;

import java.util.List;

/**
 * 函数使用方法
 * 入参3个 1. p1 内容(String) 2. p2 对比的数据(String) 3. 内容的分割符号(String)
 * 出参boolean类型 --> true OR false
 * isExist(p1,p2,p3)
 * 例如:
 * isExist("2 3 4 5 6","2"," "); 返回: true, 判断2 是否存在2 3 4 5 6 内容中,内容用空格分割
 * @author huanzhou
 * @date 2024/3/26
 */
public class DoesItExistFunction extends StringFunction {
    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        if(dataList.size() !=3){
            throw new ReportComputeException("Function ["+name()+"] need a 3 number parameter.");
        }
        String text=buildString(dataList);
        String result=null;
        ExpressionData<?> data=dataList.get(1);
        if(data instanceof ObjectListExpressionData){
            ObjectListExpressionData listData=(ObjectListExpressionData)data;
            List<?> list=listData.getData();
            if(list==null || list.size()!=1){
                throw new ReportComputeException("Function ["+name()+"] need a data of number parameter.");
            }
            Object obj=list.get(0);
            if(obj==null){
                throw new ReportComputeException("Function ["+name()+"] parameter can not be null.");
            }
            result=obj.toString();
        }else if(data instanceof ObjectExpressionData){
            ObjectExpressionData objData=(ObjectExpressionData)data;
            Object obj=objData.getData();
            if(obj==null){
                throw new ReportComputeException("Function ["+name()+"] parameter can not be null.");
            }
            result=obj.toString();
        } else if( data instanceof BindDataListExpressionData){
            BindDataListExpressionData objData = (BindDataListExpressionData)data;
            List<BindData> list = objData.getData();
            if(list==null || list.size()!=1){
                throw new ReportComputeException("Function ["+name()+"] need a data of number parameter.");
            }
            BindData obj=list.get(0);
            if(obj==null){
                throw new ReportComputeException("Function ["+name()+"] parameter can not be null.");
            }
            result=obj.getValue().toString();
        }
        else{
            throw new ReportComputeException("Function ["+name()+"] need a data of number parameter.");
        }

        ExpressionData<?> exprData=dataList.get(2);
        String index  = buildIndexFo(exprData);
        return StrUtil.split(result,index).contains(text);
    }

    private String buildIndexFo(ExpressionData<?> exprData) {
        if(exprData instanceof ObjectExpressionData){
            ObjectExpressionData objData=(ObjectExpressionData)exprData;
            Object obj=objData.getData();
            if(obj==null){
                throw new ReportComputeException("Function ["+name()+"] second parameter can not be null.");
            }
            return obj.toString();
        }
        throw new ReportComputeException("Function ["+name()+"] position data is invalid : "+exprData);
    }

    @Override
    public String name() {
        return "isExist";
    }
}
