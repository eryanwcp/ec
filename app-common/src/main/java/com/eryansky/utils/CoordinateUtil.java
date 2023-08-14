package com.eryansky.utils;

import java.math.BigDecimal;

/**
 * 功能:提供坐标偏移公共类
 * 引用浪潮方法
 *
 * @author 郭明阳
 * @date 2015/8/29
 */
public class CoordinateUtil {
	//
	// Krasovsky 1940
	//
	// a = 6378245.0, 1/f = 298.3
	// b = a * (1 - f)
	// ee = (a^2 - b^2) / a^2;
	static double a = 6378245.0;
	static double ee = 0.00669342162296594323;
	static double pi = 3.14159265358979324;
	static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;

	/**
	 * GCJ02(国家局、高德、谷歌)转换为百度坐标
	 * @param  x
	 * @param  y
	 * @return double[2] xy
	 */
	public static double[] bd_encrypt(double x,double y)
	{
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
		double[] xy = new double[2];
		xy[0] = new BigDecimal(z * Math.cos(theta) + 0.0065).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
		xy[1] = new BigDecimal(z * Math.sin(theta) + 0.006).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
		return xy;
	}

	/**
	 * 百度坐标转换为GCJ02(国家局、高德、谷歌)
	 * @param  x
	 * @param  y
	 * @return double[2] xy
	 */
	public static double[] bd_decrypt(double x,double y)
	{
		x = x - 0.0065;
		y = y - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
		double[] xy = new double[2];
		xy[0] = new BigDecimal(z * Math.cos(theta)).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
		xy[1] = new BigDecimal(z * Math.sin(theta)).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
		return xy;
	}

	private static double transLat(double x,double y){
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(x > 0 ? x:-x);
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	}

	private static double transLon(double x,double y){
		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(x > 0 ? x:-x);
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
		return ret;
	}

	private static boolean outOfChina(double x,double y){
		if (x < 72.004 || x > 137.8347)
			return true;
		if (y < 0.8293 || y > 55.8271)
			return true;
		return false;
	}

	/**
	 * 原始坐标(WGS84)转换为GCJ02(国家局、高德、谷歌)
	 * @param  x
	 * @param  y
	 * @return double[2] xy
	 */
	public static double[] WgsToGcj(double x,double y){
		double[] gcjLoc = new double[2];
		if (outOfChina(x, y))
		{
			gcjLoc[0] = x;
			gcjLoc[1] = y;
			return gcjLoc;
		}
		double dLat = transLat(x - 105.0, y - 35.0);
		double dLon = transLon(x - 105.0, y - 35.0);
		double radLat = y / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		gcjLoc[1] = new BigDecimal(y + dLat).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
		gcjLoc[0] = new BigDecimal(x + dLon).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();

		return gcjLoc;
	}

	/**
	 * GCJ02(国家局、高德、谷歌)转换为原始坐标(WGS84)
	 * @param  x
	 * @param  y
	 * @return double[2] xy
	 */
	public static double[] GcjToWgs(double x,double y)
	{
		double[] wgLoc = new double[2];
		double wgX = x, wgY = y;
		double dX,dY;
		double[] currGcLoc = new double[2];
		int maxCount = 100;
		int count = 0;
		while (true) {
			currGcLoc = WgsToGcj(wgX,wgY);
			dX = x - currGcLoc[0];
			dY = y - currGcLoc[1];
			if (Math.abs(dY) < 1e-5 && Math.abs(dX) < 1e-5) {  // 1e-6 ~ centimeter level accuracy
				// Result of experiment:
				//   Most of the time 2 iterations would be enough for an 1e-8 accuracy (milimeter level).
				//
				wgLoc[0] = new BigDecimal(wgX).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
				wgLoc[1] = new BigDecimal(wgY).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
				break;
			}
			wgX += dX;
			wgY += dY;
			if(count>maxCount){//超过100次计算未满足条件，返回0
				wgLoc[0] = 0;
				wgLoc[1] = 0;
				break;
			}
			count++;
		}
		return wgLoc;
	}


	/**
	 * 坐标转换，腾讯地图转换成百度地图坐标
	 * @param lon 腾讯经度
	 * @param lat 腾讯纬度
	 * @return 返回结果：经度,纬度
	 */
	public static double[] map_tx2bd(double lon, double lat){
		double bd_lat;
		double bd_lon;
		double x_pi=3.14159265358979324;
		double x = lon, y = lat;
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
		bd_lon = z * Math.cos(theta) + 0.0065;
		bd_lat = z * Math.sin(theta) + 0.006;

		return new double[]{bd_lon,bd_lat};
	}


	/**
	 * 坐标转换，百度地图坐标转换成腾讯地图坐标
	 * @param lon  百度坐标经度
	 * @param lat  百度坐标纬度
	 * @return 返回结果：经度,纬度
	 */
	public static double[] map_bd2tx(double lon, double lat){
		double tx_lat;
		double tx_lon;
		double x_pi=3.14159265358979324;
		double x = lon - 0.0065, y = lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
		tx_lon = z * Math.cos(theta);
		tx_lat = z * Math.sin(theta);
		return new double[]{tx_lon,tx_lat};
	}

	public static final double EARTH_RADIUS = 6378137;
	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}
	/**
	 * 根据两点间经纬度坐标（double值），计算两点间距离，单位为米
	 *
	 * @param lng1
	 * @param lat1
	 * @param lng2
	 * @param lat2
	 * @return
	 */
	public static double getDistance(double lng1, double lat1, double lng2, double lat2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
				Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}
}
