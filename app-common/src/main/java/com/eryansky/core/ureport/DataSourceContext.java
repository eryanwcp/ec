/*
 * All content copyright http://www.j2eefast.com, unless 
 * otherwise indicated. All rights reserved.
 * No deletion without permission
 */
package com.eryansky.core.ureport;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源的上下文容器
 */
public class DataSourceContext {
	
	/**
	 * 主数据源名称
	 */
	public static final String MASTER_DATASOURCE_NAME = "MASTER";//master

	/**
	 * 数据源容器
	 */
	private static Map<String, DataSource> DATA_SOURCES = new ConcurrentHashMap<>();

	/**
	 * 新增datasource
	 *
	 * @author zhouzhou
	 * @Date 2019-06-12 14:51
	 */
	public static void addDataSource(String dbName, DataSource dataSource) {
		DATA_SOURCES.put(dbName, dataSource);
	}


	/**
	 * 获取数据源
	 *
	 * @author zhouzhou
	 * @Date 2019-06-12 13:50
	 */
	public static Map<String, DataSource> getDataSources() {
		return DATA_SOURCES;
	}



	/**
	 * 获取系统缺省的数据源
	 */
	public static DataSource addDefaultDataSource(DataSource dataSource) {

		return DATA_SOURCES.put(MASTER_DATASOURCE_NAME,dataSource);
	}

	/**
	* 获取系统缺省的数据源
	 */
	public static DataSource getDefaultDataSource() {
		
		return DATA_SOURCES.get(MASTER_DATASOURCE_NAME);
	}


}
