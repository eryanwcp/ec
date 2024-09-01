/*
 * All content copyright http://www.j2eefast.com, unless
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */
package com.bstek.ureport.parser.impl;

import com.bstek.ureport.definition.ImgDefinition;
import com.bstek.ureport.definition.value.Source;
import com.bstek.ureport.expression.ExpressionUtils;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.parser.Parser;
import org.dom4j.Element;

/**
 * @author huanzhou
 * @date 2023/10/30
 */
public class ImgParser implements Parser<ImgDefinition> {

    @Override
    public ImgDefinition parse(Element element) {
        ImgDefinition imgDefinition = new ImgDefinition();
        imgDefinition.setIndex(Integer.valueOf(element.attributeValue("index")));
        imgDefinition.setWidth(Integer.valueOf(element.attributeValue("width")));
        imgDefinition.setHeight(Integer.valueOf(element.attributeValue("height")));
        imgDefinition.setTop(Integer.valueOf(element.attributeValue("top")));
        imgDefinition.setLeft(Integer.valueOf(element.attributeValue("left")));

        Source source=Source.valueOf(element.attributeValue("source"));
        imgDefinition.setSource(source);

        for(Object obj:element.elements()){
            if(obj==null || !(obj instanceof Element)){
                continue;
            }
            Element ele=(Element)obj;
            if(ele.getName().equals("simple-value")){
                imgDefinition.setPath(ele.getText());
                break;
            }else if(ele.getName().equals("expression-value")){
                imgDefinition.setExpr(ele.getText());
                break;
            }
        }

        if(source.equals(Source.expression)){
            String expr= imgDefinition.getExpr();
            Expression expression= ExpressionUtils.parseExpression(expr);
            imgDefinition.setExpression(expression);
        }

        return imgDefinition;
    }
}
