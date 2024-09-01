/*
 * All content copyright http://www.j2eefast.com, unless
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */
package com.bstek.ureport.expression.function.lang;

import cn.hutool.core.convert.NumberChineseFormatter;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.model.Cell;

import java.util.List;

/**
 * 中文金额大写转换
 * @author huanzhou
 * @date 2024/1/11
 */
public class NumberChineseFormatterFunction extends DoubleFunction{

    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        Double text = buildDouble(dataList);
        return NumberChineseFormatter.format(text, true,true,"负","圆");
    }

    @Override
    public String name() {
        return "convertAmount";
    }
}
