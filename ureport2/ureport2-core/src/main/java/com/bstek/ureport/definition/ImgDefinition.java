/*
 * All content copyright http://www.j2eefast.com, unless
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */
package com.bstek.ureport.definition;

import com.bstek.ureport.definition.value.Source;
import com.bstek.ureport.definition.value.Value;
import com.bstek.ureport.definition.value.ValueType;
import com.bstek.ureport.expression.model.Expression;

import java.io.Serializable;

/**
 * 图片定义类
 * @author huanzhou
 * @date 2023/10/30
 */
public class ImgDefinition implements Serializable{

    private static final long serialVersionUID = -7239528314017768029L;

    private int index;
    private int top;
    private int left;
    private String path;
    private String expr;
    private Expression expression;
    private Source source;
    private int width;
    private int height;
    private int realTop = 0;
    private boolean isRealTp = false;
    private String base64;

    public String getValue() {
        if(!this.source.equals(Source.expression)){
            return path;
        }else{
            return expr;
        }
    }

    public boolean isRealTp() {
        return isRealTp;
    }

    public void setRealTp(boolean realTp) {
        isRealTp = realTp;
    }

    public int getRealTop() {
        return realTop;
    }

    public void setRealTop(int realTop) {
        this.realTop = realTop;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
//    public void setValue(String value) {
//        this.value = value;
//    }

    public int getTop() {
        return top;
    }
    public void setTop(int top) {
        this.top = top;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public void setSource(Source source) {
        this.source = source;
    }
    public Source getSource() {
        return source;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public void setExpr(String expr) {
        this.expr = expr;
    }
    public String getExpr() {
        return expr;
    }
    public Expression getExpression() {
        return expression;
    }
    public void setExpression(Expression expression) {
        this.expression = expression;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
}
