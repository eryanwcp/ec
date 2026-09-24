
package com.eryansky.common.mail.config;

import java.util.Properties;

/**
 * 存储用户配置对于系统配置信息
 *
 * @author Eryan
 * @date 2015-09-14
 */
public interface Configuration {
	/**
	 * 工厂方法，创建属性集实体
	 * @return 属性集实体类，存储属性键值对
	 */
	Properties makeProperties();
}
