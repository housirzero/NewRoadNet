package team.support.function;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import team.net.graph.LngLat;

public class Distance{
	public static void main(String[] args) {

		System.out.println( lngLatDistance( 116.42,	39.9, 116.432, 39.9) );
		System.out.println( lngLatDistance( 116.55,	39.929, 116.55, 39.920) );
		// 纬度 0.009 == 1000m
		// 经度 0.012 == 1000m
	}
	
	private static double EARTH_RADIUS = 6378137;
	private static double rad(double d)
	{
		return d*Math.PI/180.0;
	}
	public static double lngLatDistance(double lon1,double lat1,double lon2,double lat2)
	{
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1-radLat2;
		double b =rad(lon1)-rad(lon2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
		return Math.abs(s*EARTH_RADIUS);
	}
	
	public static double lngLatDistance(LngLat lngLat1,LngLat lngLat2)
	{
		double lon1 = lngLat1.lng;
		double lon2 = lngLat2.lng;
		double lat1 = lngLat1.lat;
		double lat2 = lngLat2.lat;
		
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1-radLat2;
		double b =rad(lon1)-rad(lon2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
		return Math.abs(s*EARTH_RADIUS);
	}

	public static double lngLatDistanceInNum(LngLat lngLat1,LngLat lngLat2)
	{
		return lngLatDistanceInNum(lngLat1.lng, lngLat1.lat, lngLat2.lng, lngLat2.lat );
	}
	public static double lngLatDistanceInNum(double lon1,double lat1,double lon2,double lat2)
	{
		double lngDt = lon1-lon2;
		double latDt = lat1-lat2;
//		return (lngDt*lngDt + latDt*latDt); // 都在北京市内，范围相对于地球来说不算大，可以近似用勾股定理计算距离
		return Math.abs(lngDt)+Math.abs(latDt);
	}
//	public static double lngLatDistanceInNum(double lon1,double lat1,double lon2,double lat2)
//	{
//		double lngDt = (lon1-lon2)*(lon1-lon2);
//		double latDt = (lat1-lat2)*(lat1-lat2);
////		return (lngDt*lngDt + latDt*latDt); // 都在北京市内，范围相对于地球来说不算大，可以近似用勾股定理计算距离
//		
//		return Math.sqrt(lngDt+latDt);
//	}
	/**
	 * 两个时间相差的秒数（s）
	 * return : t1 - t2
	 */
	public static int timeDistance( Date t1, Date t2 )
	{
		int dt =  (int)(t1.getTime()-t2.getTime())/1000;
		return dt;
	}
	
	/**
	 * 两个时间相差的秒数（s）<br>
	 * <br>
	 * Example : <br>
	 * 		<B>SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");</B><br>
	 * 		<B>SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");</B><br>
	 * 		<B>SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");</B><br>
	 * <br>
	 * return : t1 - t2
	 */
	public static int timeDistance( String t1Str, String t2Str, SimpleDateFormat sdf )
	{
		Date t1 = null;
		Date t2 = null;
		try {
			t1 = sdf.parse(t1Str);
			t2 = sdf.parse(t2Str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int dt =  (int)(t1.getTime()-t2.getTime())/1000;
		return dt;
	}

}