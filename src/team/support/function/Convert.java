package team.support.function;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import team.net.graph.LngLat;
import cn.zhugefubin.maptool.ConverterTool;
import cn.zhugefubin.maptool.Point;

/**
 * ת����غ���
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
	 * �ж��Ƿ�Ϊ���� 
	 * @param str ������ַ��� 
	 * @return ����������true,���򷵻�false 
	 */
	public static boolean isInteger(String str)
	{
		if(str == null || str.length() == 0)
			return false;
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}
	
	/**
	 * ���ַ���ת��Ϊ�������͵��ַ���(�༴ȥ��ǰ׺0)
	 * �粻��ת��Ϊ���֣��򷵻��ַ�������
	 * @param str
	 * @return
	 */
	public static String praseInt(String str)
	{
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		if( pattern.matcher(str).matches() ) // ����ת��
			return Integer.parseInt(str)+"";
		return str; // ����ת������������
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
	 * ��ʱ���Ϊ�ļ���
	 * ����ȡʱ��ֻ��ȷ����
	 * input : 2014-08-01 12:56:63
	 * output : 20140801
	 * @return
	 */
	public static String timeToFileName( Date date )
	{
		SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(date);
	}

	/* ʾ������
	 * ʵ�ְٶ����꣨bd09ll���͹ȸ����꣨gcj02����ת
	 * �÷������������ϵ�һ��C���԰汾���Ҹ���д��Java��
	 *  ������ת�������ƫ���Լ��10��֮�ڣ�Ӧ�ÿ������������������
	 */
	
	/**
	 * �ٶ�--�ȸ裨���������ٶȾ�γ�ȡ���
	 * @return
	 */
	public static LngLat BD2GG( LngLat lngLat )
	{
		ConverterTool ct = new ConverterTool();
		Point p = ct.BD2GG(lngLat.lng, lngLat.lat); // ת�������Point����
		return new LngLat(p.getLongitude(), p.getLatitude());
	}
	
	/**
	 * �ȸ赽�ٶȣ����������ȸ辭γ�ȡ���
	 * @return
	 */
	public static LngLat GG2BD( LngLat lngLat )
	{
		// ʵ����ConverterTool�࣬��Ҳ�����Լ�����cn.zhugefubin.maptool.Converter��ķ���
		ConverterTool ct = new ConverterTool();
		Point p = ct.GG2BD(lngLat.lng, lngLat.lat); // ת�������Point����
		return new LngLat(p.getLongitude(), p.getLatitude());
	}
	
	/**
	 * GPS--�ȸ裨��������GPS��γ�ȡ���
	 * @return
	 */
	public static LngLat GPS2GG( LngLat lngLat )
	{
		ConverterTool ct = new ConverterTool();
		Point p = ct.GPS2GG(lngLat.lng, lngLat.lat); // ת�������Point����
		return new LngLat(p.getLongitude(), p.getLatitude());
	}
	
	public static void main(String[] args){
		LngLat lngLat = new LngLat(116.347483,39.981486);
		System.out.println(GG2BD(GPS2GG(lngLat)).toString());
	}
}
