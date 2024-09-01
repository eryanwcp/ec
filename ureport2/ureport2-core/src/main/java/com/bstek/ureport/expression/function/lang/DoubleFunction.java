/*
 * All content copyright http://www.j2eefast.com, unless
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */
package com.bstek.ureport.expression.function.lang;

import cn.hutool.core.convert.Convert;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.expression.function.Function;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.expression.model.data.ObjectExpressionData;
import com.bstek.ureport.expression.model.data.ObjectListExpressionData;
import java.util.List;

/**
 * @author huanzhou
 * @date 2024/1/11
 */
public abstract class DoubleFunction implements Function {
    protected Double buildDouble(List<ExpressionData<?>> dataList) {
        if(dataList==null || dataList.size()==0){
            throw new ReportComputeException("Function ["+name()+"] need a data of string parameter.");
        }
        Double result = null;
        ExpressionData<?> data=dataList.get(0);
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
            result= Convert.toDouble(obj);
        }else if(data instanceof ObjectExpressionData){
            ObjectExpressionData objData=(ObjectExpressionData)data;
            Object obj=objData.getData();
            if(obj==null){
                throw new ReportComputeException("Function ["+name()+"] parameter can not be null.");
            }
            result= Convert.toDouble(obj);
        }else{
            throw new ReportComputeException("Function ["+name()+"] need a data of number parameter.");
        }
        return result;
    }
}
