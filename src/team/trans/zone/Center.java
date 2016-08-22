package team.trans.zone;

import team.net.graph.LngLat;

/**
 * 交通小区的质心
 */
public class Center
{
	public int zoneId;
	public LngLat lngLat;
	
	public Center(String zoneId, String lng, String lat)
	{
		this.zoneId = Integer.parseInt(zoneId);
		this.lngLat = new LngLat(lng, lat);
	}
}
