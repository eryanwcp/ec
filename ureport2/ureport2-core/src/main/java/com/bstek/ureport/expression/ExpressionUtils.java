/*******************************************************************************
 * Copyright 2017 Bstek
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.ureport.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.convert.NumberChineseFormatter;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.expression.function.ParameterFunction;
import com.bstek.ureport.expression.function.date.DateFunction;
import com.bstek.ureport.expression.function.lang.NumberChineseFormatterFunction;
import com.bstek.ureport.expression.function.string.SubstringFunction;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.expression.model.data.ObjectExpressionData;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.bstek.ureport.build.assertor.Assertor;
import com.bstek.ureport.build.assertor.EqualsAssertor;
import com.bstek.ureport.build.assertor.EqualsGreatThenAssertor;
import com.bstek.ureport.build.assertor.EqualsLessThenAssertor;
import com.bstek.ureport.build.assertor.GreatThenAssertor;
import com.bstek.ureport.build.assertor.InAssertor;
import com.bstek.ureport.build.assertor.LessThenAssertor;
import com.bstek.ureport.build.assertor.LikeAssertor;
import com.bstek.ureport.build.assertor.NotEqualsAssertor;
import com.bstek.ureport.build.assertor.NotInAssertor;
import com.bstek.ureport.dsl.ReportParserLexer;
import com.bstek.ureport.dsl.ReportParserParser;
import com.bstek.ureport.exception.ReportParseException;
import com.bstek.ureport.expression.function.Function;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.expression.model.Op;
import com.bstek.ureport.expression.parse.ExpressionErrorListener;
import com.bstek.ureport.expression.parse.ExpressionVisitor;
import com.bstek.ureport.expression.parse.builder.BooleanExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.CellObjectExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.CellPositionExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.CurrentCellDataExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.CurrentCellValueExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.DatasetExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.ExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.FunctionExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.IntegerExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.NullExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.NumberExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.RelativeCellExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.SetExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.StringExpressionBuilder;
import com.bstek.ureport.expression.parse.builder.VariableExpressionBuilder;

import javax.swing.*;

/**
 * @author Jacky.gao
 * @since 2016年12月24日
 */
public class ExpressionUtils implements ApplicationContextAware{
	public static final String EXPR_PREFIX="${";
	public static final String EXPR_SUFFIX="}";
	private static ExpressionVisitor exprVisitor;
	private static Map<String,Function> functions=new HashMap<String,Function>();
	private static Map<Op,Assertor> assertorsMap=new HashMap<Op,Assertor>();
	private static List<ExpressionBuilder> expressionBuilders=new ArrayList<ExpressionBuilder>();
	private static List<String> cellNameList=new ArrayList<String>();
	private static String[] LETTERS={"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
	static{
		expressionBuilders.add(new StringExpressionBuilder());
		expressionBuilders.add(new VariableExpressionBuilder());
		expressionBuilders.add(new BooleanExpressionBuilder());
		expressionBuilders.add(new IntegerExpressionBuilder());
		expressionBuilders.add(new DatasetExpressionBuilder());
		expressionBuilders.add(new FunctionExpressionBuilder());
		expressionBuilders.add(new NumberExpressionBuilder());
		expressionBuilders.add(new CellPositionExpressionBuilder());
		expressionBuilders.add(new RelativeCellExpressionBuilder());
		expressionBuilders.add(new SetExpressionBuilder());
		expressionBuilders.add(new CellObjectExpressionBuilder());
		expressionBuilders.add(new NullExpressionBuilder());
		expressionBuilders.add(new CurrentCellValueExpressionBuilder());
		expressionBuilders.add(new CurrentCellDataExpressionBuilder());
		
		assertorsMap.put(Op.Equals, new EqualsAssertor());
		assertorsMap.put(Op.EqualsGreatThen, new EqualsGreatThenAssertor());
		assertorsMap.put(Op.EqualsLessThen, new EqualsLessThenAssertor());
		assertorsMap.put(Op.GreatThen, new GreatThenAssertor());
		assertorsMap.put(Op.LessThen, new LessThenAssertor());
		assertorsMap.put(Op.NotEquals, new NotEqualsAssertor());
		assertorsMap.put(Op.In, new InAssertor());
		assertorsMap.put(Op.NotIn, new NotInAssertor());
		assertorsMap.put(Op.Like, new LikeAssertor());
		
		for(int i=0;i<LETTERS.length;i++){
			cellNameList.add(LETTERS[i]);
		}
		
		for(int i=0;i<LETTERS.length;i++){
			String name=LETTERS[i];
			for(int j=0;j<LETTERS.length;j++){
				cellNameList.add(name+LETTERS[j]);
			}
		}
	}
	
	public static List<String> getCellNameList() {
		return cellNameList;
	}
	
	public static Map<String, Function> getFunctions() {
		return functions;
	}
	
	public static Map<Op, Assertor> getAssertorsMap() {
		return assertorsMap;
	}
	
	public static boolean conditionEval(Op op,Object left,Object right){
		Assertor assertor=assertorsMap.get(op);
		boolean result=assertor.eval(left, right);
		return result;
	}
	
	public static Expression parseExpression(String text){
		// 表达式
		ANTLRInputStream antlrInputStream=new ANTLRInputStream(text);
		// 构造词法分析器 lexer
		ReportParserLexer lexer=new ReportParserLexer(antlrInputStream);
		//用词法分析器 lexer 构造一个记号流 tokens
		CommonTokenStream tokenStream=new CommonTokenStream(lexer);
		//  再使用 tokens 构造语法分析器 parser,至此已经完成词法分析和语法分析的准备工作
		ReportParserParser parser=new ReportParserParser(tokenStream);
		ExpressionErrorListener errorListener=new ExpressionErrorListener();
		parser.addErrorListener(errorListener);
		exprVisitor=new ExpressionVisitor(expressionBuilders);
		Expression expression=exprVisitor.visitEntry(parser.entry());
		String error=errorListener.getErrorMessage();
		if(error!=null){
			throw new ReportParseException("Expression parse error:"+error);
		}
		return expression;
	}
	
	public static ExpressionVisitor getExprVisitor() {
		return exprVisitor;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Collection<Function> coll=applicationContext.getBeansOfType(Function.class).values();
		for(Function fun:coll){
			functions.put(fun.name(), fun);
		}
	}

	public static void main(String[] args) {
		String var = "if(param(\"completedValue\") / param(\"targetValue\") >= 1){\n" +
				"\t\treturn 100;\n" +
				"}else if(param(\"completedValue\") / param(\"targetValue\") < 1 && param(\"completedValue\") / param(\"targetValue\") >= 0.9){\n" +
				"\treturn convertAmount(param(\"strValue\"));\n" +
				"}else if(param(\"completedValue\") / param(\"targetValue\") < 0.9 && param(\"completedValue\") / param(\"targetValue\") >= 0.8){\n" +
				"\treturn 80;\n" +
				"}else{\n" +
				"\treturn 0;\n" +
				"}";
		DateFunction dateFunction = new DateFunction();
		ParameterFunction parameterFunction = new ParameterFunction();
		SubstringFunction substringFunction = new SubstringFunction();
		NumberChineseFormatterFunction numberChineseFormatterFunction = new NumberChineseFormatterFunction();
		functions.put(dateFunction.name(),dateFunction);
		functions.put(parameterFunction.name(),parameterFunction);
		functions.put(substringFunction.name(),substringFunction);
		functions.put(numberChineseFormatterFunction.name(),numberChineseFormatterFunction);
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put("completedValue",450);
		parameterMap.put("targetValue",500);
		parameterMap.put("strValue",30094.44);
		Context context=new Context(null,parameterMap);

		Expression expr = ExpressionUtils.parseExpression(var);
		ExpressionData<?> exprData = expr.execute(null,null,context);
		if(exprData instanceof ObjectExpressionData){
			ObjectExpressionData data=(ObjectExpressionData)exprData;
			System.out.println(data.getData());
//			if((Boolean) data.getData()){
//				System.out.println("真");
//			}else {
//				System.out.println("假");
//			}
		}

//		System.out.println(NumberChineseFormatter.format(3004.44, true,true,"负","圆"));
	}
}
