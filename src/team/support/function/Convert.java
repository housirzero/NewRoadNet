package team.support.function;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import team.net.graph.LngLat;
import cn.zhugefubin.maptool.ConverterTool;
import cn.zhugefubin.maptool.Point;

/**
 * 转换相关函数
 * @author DELL
 *
 */
public class Convert {
	
	public static String secondsToTime( int seconds )
	{
		int second = seconds % 60;
		int minute = (seconds / 60) % 60;
		int hour = seconds / 3600;
		return hour + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second);
	}
	
	/**
	 * 判断是否为整数 
	 * @param str 传入的字符串 
	 * @return 是整数返回true,否则返回false 
	 */
	public static boolean isInteger(String str)
	{
		if(str == null || str.length() == 0)
			return false;
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}
	
	/**
	 * 将字符串转换为数字类型的字符串(亦即去掉前缀0)
	 * 如不能转换为数字，则返回字符串本身
	 * @param str
	 * @return
	 */
	public static String praseInt(String str)
	{
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		if( pattern.matcher(str).matches() ) // 可以转换
			return Integer.parseInt(str)+"";
		return str; // 不能转换，返回自身
	}

	public static Date getDateCopy( Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return sdf.parse( sdf.format(date) );
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 将时间改为文件名
	 * 即获取时间只精确到天
	 * input : 2014-08-01 12:56:63
	 * output : 20140801
	 * @return
	 */
	public static String timeToFileName( Date date )
	{
		SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(date);
	}

	/* 示例代码
	 * 实现百度坐标（bd09ll）和谷歌坐标（gcj02）互转
	 * 该方法来自于网上的一个C语言版本，我给改写成Java了
	 *  经测试转换后左边偏差大约在10米之内，应该可以满足基本开发需求
	 */
	
	/**
	 * 百度--谷歌（参数：“百度经纬度”）
	 * @return
	 */
	public static LngLat BD2GG( LngLat lngLat )
	{
		ConverterTool ct = new ConverterTool();
		Point p = ct.BD2GG(lngLat.lng, lngLat.lat); // 转换结果用Point接收
		return new LngLat(p.getLongitude(), p.getLatitude());
	}
	
	/**
	 * 谷歌到百度（参数：“谷歌经纬度”）
	 * @return
	 */
	public static LngLat GG2BD( LngLat lngLat )
	{
		// 实例化ConverterTool类，你也可以自己调用cn.zhugefubin.maptool.Converter里的方法
		ConverterTool ct = new ConverterTool();
		Point p = ct.GG2BD(lngLat.lng, lngLat.lat); // 转换结果用Point接收
		return new LngLat(p.getLongitude(), p.getLatitude());
	}
	
	/**
	 * GPS--谷歌（参数：“GPS经纬度”）
	 * @return
	 */
	public static LngLat GPS2GG( LngLat lngLat )
	{
		ConverterTool ct = new ConverterTool();
		Point p = ct.GPS2GG(lngLat.lng, lngLat.lat); // 转换结果用Point接收
		return new LngLat(p.getLongitude(), p.getLatitude());
	}
	
	public static void main(String[] args){
		LngLat lngLat = new LngLat(116.347483,39.981486);
		System.out.println(GG2BD(GPS2GG(lngLat)).toString());
	}
}
