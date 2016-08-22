package gps;
/**
 * ����ͼAPI����ϵͳ�Ƚ���ת��;
 * <br>
 * WGS84����ϵ������������ϵ��������ͨ�õ�����ϵ���豸һ�����GPSоƬ���߱���оƬ��ȡ�ľ�γ��ΪWGS84��������ϵ,
 * �ȸ��ͼ���õ���WGS84��������ϵ���й���Χ���⣩;
 * <br>
 * GCJ02����ϵ������������ϵ�������й����Ҳ����ƶ��ĵ�����Ϣϵͳ������ϵͳ����WGS84����ϵ�����ܺ������ϵ��
 * �ȸ��й���ͼ�������й���ͼ���õ���GCJ02��������ϵ; 
 * <br>
 * BD09����ϵ�����ٶ�����ϵ��GCJ02����ϵ�����ܺ������ϵ;
 * <br>
 * �ѹ�����ϵ��ͼ������ϵ�ȣ�����Ҳ����GCJ02�����ϼ��ܶ��ɵġ�
 * <br>
 * 
 * BaseInformation/SubwayStation2015.csv �Լ� BusStation2015v2.csv ����84����ϵ : WGS84 --> GCJ02 --> BD09
 */
public class GPSConveter {

	private static double pi = 3.14159265358979324D;// Բ����
	private static double a = 6378245.0D;// WGS ����뾶
	private static double ee = 0.00669342162296594323D;// WGS ƫ���ʵ�ƽ��

	/**
	 * 84 to ��������ϵ (GCJ-02) World Geodetic System ==> Mars Geodetic System
	 */
	public static Gps wgs84_To_Gcj02(double lat, double lon) {
		if (outOfChina(lat, lon)) {
			return null;
		}
		double dLat = transformLat(lon - 105.0, lat - 35.0);
		double dLon = transformLon(lon - 105.0, lat - 35.0);
		double radLat = lat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		double mgLat = lat + dLat;
		double mgLon = lon + dLon;
		return new Gps(mgLat, mgLon);
	}

	/**
	 * * ��������ϵ (GCJ-02) to 84
	 * */
	public static Gps gcj02_To_Gps84(double lat, double lon) {
		Gps gps = transform(lat, lon);
		double longitude = lon * 2 - gps.getWgLon();
		double latitude = lat * 2 - gps.getWgLat();
		return new Gps(latitude, longitude);
	}

	/**
	 * ��������ϵ (GCJ-02) ��ٶ�����ϵ (BD-09) ��ת���㷨 �� GCJ-02 ����ת���� BD-09 ����
	 */
	public static Gps gcj02_To_Bd09(double gg_lat, double gg_lon) {
		double x = gg_lon, y = gg_lat;
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * pi);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * pi);
		double bd_lon = z * Math.cos(theta) + 0.0065;
		double bd_lat = z * Math.sin(theta) + 0.006;
		return new Gps(bd_lat, bd_lon);
	}

	/**
	 * * ��������ϵ (GCJ-02) ��ٶ�����ϵ (BD-09) ��ת���㷨 * * �� BD-09 ����ת����GCJ-02 ����
	 */
	public static Gps bd09_To_Gcj02(double bd_lat, double bd_lon) {
		double x = bd_lon - 0.0065, y = bd_lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
		double gg_lon = z * Math.cos(theta);
		double gg_lat = z * Math.sin(theta);
		return new Gps(gg_lat, gg_lon);
	}

	/**
	 * (BD-09)-->84
	 */
	public static Gps bd09_To_Gps84(double bd_lat, double bd_lon) {

		Gps gcj02 = bd09_To_Gcj02(bd_lat, bd_lon);
		Gps map84 = gcj02_To_Gps84(gcj02.getWgLat(),
				gcj02.getWgLon());
		return map84;

	}

	public static boolean outOfChina(double lat, double lon) {
		if (lon < 72.004 || lon > 137.8347)
			return true;
		if (lat < 0.8293 || lat > 55.8271)
			return true;
		return false;
	}

	public static Gps transform(double lat, double lon) {
		if (outOfChina(lat, lon)) {
			return new Gps(lat, lon);
		}
		double dLat = transformLat(lon - 105.0, lat - 35.0);
		double dLon = transformLon(lon - 105.0, lat - 35.0);
		double radLat = lat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		double mgLat = lat + dLat;
		double mgLon = lon + dLon;
		return new Gps(mgLat, mgLon);
	}

	public static double transformLat(double x, double y) {
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
				+ 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	}

	public static double transformLon(double x, double y) {
		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
				* Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
				* pi)) * 2.0 / 3.0;
		return ret;
	}
	
	public static Gps wgs84_To_Bd09(double lat, double lon) {
		Gps gps = new Gps(lat, lon);
		Gps gcj = wgs84_To_Gcj02(gps.getWgLat(), gps.getWgLon());
		Gps bd = gcj02_To_Bd09(gcj.getWgLat(), gcj.getWgLon());
		return bd;
	}
  
    public static void main(String[] args) {  
		Gps gps = new Gps(39.906143,116.184434);
		Gps gcj = wgs84_To_Gcj02(gps.getWgLat(), gps.getWgLon());
		System.out.println("gcj :" + gcj);
		Gps bd = gcj02_To_Bd09(gcj.getWgLat(), gcj.getWgLon());
		System.out.println("bd  :" + bd);
	}
}
