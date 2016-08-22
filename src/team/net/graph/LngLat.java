package team.net.graph;

import gps.GPSConveter;
import gps.Gps;

/**
 * ��γ��
 */
public class LngLat {
	/**
	 * ����
	 */
	public double lng;
	/**
	 * γ��
	 */
	public double lat;

	public LngLat() {

	}

	public LngLat(double lng, double lat) {
		super();
		this.lng = lng;
		this.lat = lat;
	}

	public LngLat(String lng, String lat) {
		super();
		this.lng = Double.parseDouble(lng);
		this.lat = Double.parseDouble(lat);
	}

	public LngLat Copy() {
		return new LngLat(lng, lat);
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	/**
	 * ���ļ���洢��String
	 * 
	 * @return
	 */
	public String toString() {
		return lng + "," + lat;
	}
	
	/**
	 * 
	 * @return
	 */
	public String toBaidu() {
		Gps bd = GPSConveter.wgs84_To_Bd09(lat, lng);
		return String.format("%.6f,%.6f", bd.getWgLon(), bd.getWgLat());
	}
}
