package com.eryansky.core.aop.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API访问频率限制
 * 注：仅限返回值为Result的方法添加盖注解
 * @author Eryan
 * @date : 2021-11-05
*/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiterApi {

	/**
	 * 是否启用 默认值：true
	 * <br/>支持SpEL表达式
	 * @return
	 */
	String enable() default "true";

	/**
	 * 缓存key
	 *
	 * @return
	 */
	String region() default "lock_limit_key_timeout_default";

	/**
	 * 全局范围或session范围
	 * @return
	 */
	boolean scopeAll() default false;

	/**
	 * 业务KEY
	 * @return
	 */
	String paramKey() default "CDPathSta";

	/**
	 * 访问次数
	 * @return
	 */
	int frequency() default 500;

	/**
	 * 计时周期(无效配置，请在缓存中配置caffeine.properties，对应region) 单位：秒
	 * @return
	 */
	@Deprecated
	long timeout() default 60;
		
}
