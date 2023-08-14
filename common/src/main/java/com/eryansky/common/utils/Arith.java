/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 进行BigDecimal对象的加减乘除，四舍五入等运算的工具类
 * 
 * @author Eryan
 * 
 */
public class Arith {

	/**
	 * 由于Java的简单类型不能够精确的对浮点数进行运算，这个工具类提供精 确的浮点数运算，包括加减乘除和四舍五入。
	 */
	// 默认除法运算精度
	private static final int DEF_DIV_SCALE = 10;

	// 这个类不能实例化
	private Arith() {
		super();
	}

	/**
	 * 提供精确的加法运算。
	 * 
	 * @param v1
	 *            被加数
	 * @param v2
	 *            加数
	 * @return 两个参数的和
	 */
	public static double add(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2).doubleValue();
	}

	/**
	 * 提供精确的减法运算。
	 * 
	 * @param v1
	 *            被减数
	 * @param v2
	 *            减数
	 * @return 两个参数的差
	 */
	public static double sub(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.subtract(b2).doubleValue();
	}

	/**
	 * 提供精确的乘法运算。
	 * 
	 * @param v1
	 *            被乘数
	 * @param v2
	 *            乘数
	 * @return 两个参数的积
	 */
	public static double mul(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.multiply(b2).doubleValue();
	}

	/**
	 * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入。
	 * 
	 * @param v1
	 *            被除数
	 * @param v2
	 *            除数
	 * @return 两个参数的商
	 */
	public static double div(double v1, double v2) {
		return div(v1, v2, DEF_DIV_SCALE);
	}

	/**
	 * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度，以后的数字四舍五入。
	 * 
	 * @param v1
	 *            被除数
	 * @param v2
	 *            除数
	 * @param scale
	 *            表示表示需要精确到小数点以后几位。
	 * @return 两个参数的商
	 */
	public static double div(double v1, double v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 提供精确的小数位四舍五入处理。
	 * 
	 * @param v
	 *            需要四舍五入的数字
	 * @param scale
	 *            小数点后保留几位
	 * @return 四舍五入后的结果
	 */
	public static double round(double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 提供精确的类型转换(Float)
	 * 
	 * @param v
	 *            需要被转换的数字
	 * @return 返回转换结果
	 */
	public static float convertsToFloat(double v) {
		BigDecimal b = new BigDecimal(v);
		return b.floatValue();
	}

	/**
	 * 提供精确的类型转换(Int)不进行四舍五入
	 * 
	 * @param v
	 *            需要被转换的数字
	 * @return 返回转换结果
	 */
	public static int convertsToInt(double v) {
		BigDecimal b = new BigDecimal(v);
		return b.intValue();
	}

	/**
	 * 提供精确的类型转换(Long)
	 * 
	 * @param v
	 *            需要被转换的数字
	 * @return 返回转换结果
	 */
	public static long convertsToLong(double v) {
		BigDecimal b = new BigDecimal(v);
		return b.longValue();
	}

	/**
	 * 返回两个数中大的一个值
	 * 
	 * @param v1
	 *            需要被对比的第一个数
	 * @param v2
	 *            需要被对比的第二个数
	 * @return 返回两个数中大的一个值
	 */
	public static double returnMax(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.max(b2).doubleValue();
	}

	/**
	 * 返回两个数中小的一个值
	 * 
	 * @param v1
	 *            需要被对比的第一个数
	 * @param v2
	 *            需要被对比的第二个数
	 * @return 返回两个数中小的一个值
	 */
	public static double returnMin(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.min(b2).doubleValue();
	}

	/**
	 * 精确对比两个数字
	 * 
	 * @param v1
	 *            需要被对比的第一个数
	 * @param v2
	 *            需要被对比的第二个数
	 * @return 如果两个数一样则返回0，如果第一个数比第二个数大则返回1，反之返回-1
	 */
	public static int compareTo(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.compareTo(b2);
	}

	/**
	 * BigDecimal的加法运算封装
	 * @param b1
	 * @param bn
	 * @return
	 */
	public static BigDecimal safeAdd(BigDecimal b1, BigDecimal... bn) {
		if (null == b1) {
			b1 = BigDecimal.ZERO;
		}
		if (null != bn) {
			for (BigDecimal b : bn) {
				b1 = b1.add(null == b ? BigDecimal.ZERO : b);
			}
		}
		return b1;
	}

	/**
	 * Integer加法运算的封装
	 * @param b1   第一个数
	 * @param bn   需要加的加法数组
	 * @注 ： Optional  是属于com.google.common.base.Optional<T> 下面的class
	 * @return
	 */
	public static Integer safeAdd(Integer b1, Integer... bn) {
		if (null == b1) {
			b1 = 0;
		}
		Integer r = b1;
		if (null != bn) {
			for (Integer b : bn) {
				r += Optional.ofNullable(b).orElse((0));
			}
		}
		return r > 0 ? r : 0;
	}

	/**
	 * null转0
	 * @param b1
	 * @return
	 */
	public static BigDecimal nullToZero(BigDecimal b1) {
		if (null == b1) {
			b1 = BigDecimal.ZERO;
		}
		return b1;
	}

	/**
	 * 计算金额方法
	 * @param b1
	 * @param bn
	 * @return
	 */
	public static BigDecimal safeSubtract(BigDecimal b1, BigDecimal... bn) {
		return safeSubtract(true, b1, bn);
	}

	/**
	 * BigDecimal的安全减法运算
	 * @param isZero  减法结果为负数时是否返回0，true是返回0（金额计算时使用），false是返回负数结果
	 * @param b1		   被减数
	 * @param bn        需要减的减数数组
	 * @return
	 */
	public static BigDecimal safeSubtract(Boolean isZero, BigDecimal b1, BigDecimal... bn) {
		if (null == b1) {
			b1 = BigDecimal.ZERO;
		}
		BigDecimal r = b1;
		if (null != bn) {
			for (BigDecimal b : bn) {
				r = r.subtract((null == b ? BigDecimal.ZERO : b));
			}
		}
		return isZero ? (r.compareTo(BigDecimal.ZERO) == -1 ? BigDecimal.ZERO : r) : r;
	}

	/**
	 * 整型的减法运算，小于0时返回0
	 * @param b1
	 * @param bn
	 * @return
	 */
	public static Integer safeSubtract(Integer b1, Integer... bn) {
		if (null == b1) {
			b1 = 0;
		}
		Integer r = b1;
		if (null != bn) {
			for (Integer b : bn) {
				r -= Optional.ofNullable(b).orElse((0));
			}
		}
		return null != r && r > 0 ? r : 0;
	}

	/**
	 * 金额除法计算，返回2位小数（具体的返回多少位大家自己看着改吧）
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static <T extends Number> BigDecimal safeDivide(T b1, T b2){
		return safeDivide(b1, b2, null,BigDecimal.ZERO);
	}

	/**
	 * BigDecimal的除法运算封装，如果除数或者被除数为0，返回默认值
	 * 默认返回小数位后2位，用于金额计算
	 * @param b1
	 * @param b2
	 * @param scale 精度 默认值：2
	 * @param defaultValue
	 * @return
	 */
	public static <T extends Number> BigDecimal safeDivide(T b1, T b2,Integer scale, BigDecimal defaultValue) {
		if (null == b1 || null == b2) {
			return defaultValue;
		}
		try {
			return BigDecimal.valueOf(b1.doubleValue()).divide(BigDecimal.valueOf(b2.doubleValue()), null == scale ? 2:scale, BigDecimal.ROUND_HALF_UP);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * BigDecimal的乘法运算封装
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static <T extends Number> BigDecimal safeMultiply(T b1, T b2) {
		return safeMultiply(b1,b2,null);
	}

	/**
	 * BigDecimal的乘法运算封装
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static <T extends Number> BigDecimal safeMultiply(T b1, T b2,Integer scale) {
		if (null == b1 || null == b2) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(b1.doubleValue()).multiply(BigDecimal.valueOf(b2.doubleValue())).setScale( null == scale ? 2:scale, BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * 判断是否为空或0
	 * @return
	 */
	public static boolean isNullOrZero(BigDecimal v) {
		return null == v || BigDecimal.ZERO.compareTo(v) == 0;
	}

}