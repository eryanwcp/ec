package com.eryansky.core.excels;

import bsh.Interpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eryan
 * @date : 2014-07-31 20:36
 */
public class FormulaProcessor {

	private static final Logger logger = LoggerFactory.getLogger(FormulaProcessor.class);

	private Pattern pattern = Pattern.compile("\\$(\\d+)");

	/**
	 * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
	 */
	public static final class Static {
		public static final FormulaProcessor self = new FormulaProcessor();
	}

	private FormulaProcessor(){
		
	}
	
	public static FormulaProcessor getInstance(){
		return Static.self;
	}
	
	public void fillValue(TableDataRow row){
		HashSet<Integer> computed = new HashSet<Integer>();
		for (TableDataCell cell:row.getCells()){
			TableColumn thc = row.getTable().getTableHeader().getColumnAt(cell.getColumnIndex());
			int type = thc.getColumnType();
			if (type == TableColumn.COLUMN_TYPE_FORMULA && thc.getAggregateRule() != null){
				String f = convertFormula(thc.getAggregateRule());
				try{
					Interpreter process = new Interpreter();
					process.getNameSpace().importCommands("com.eryansky.core.excels");
					process.set("row", row);
					process.set("computed", computed);
					
					Object ret = process.eval(f);
					double v = (Double)ret;
					cell.setValue(v);
					computed.add(cell.getColumnIndex());
				} catch (Exception e){
					logger.error(e.getMessage(), e);
					cell.setValue(0);
					cell.setValue("-");
					computed.add(cell.getColumnIndex());
				}
			}
		}
	}
	
	private String convertFormula(String formula){
		Matcher m = pattern.matcher(formula);
		StringBuffer sb = new StringBuffer();
		while(m.find()){
			m.appendReplacement(sb, "getValue(row, " + m.group(1) + ")");
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
}
