package team.trans.zone;

import team.net.graph.LngLat;

/**
 * 未使用交通小区的聚类结果
 * @author DELL
 *
 */
public class ClusterRecord
{
	public String stationName;
	public LngLat lngLat;
	// 下面两个是int型，但由于不会用到，所以以字符串记录，节省计算资源
	public int passengerFlowAll;
	public String timeFlow;
	/**
	 * 聚类号
	 */
	public int classId;
	/**
	 * 所属交通小区区号
	 */
	public int realZoneId;
	
	/**
	 * 少数服从多数后的ZoneId
	 */
	public int zoneId;
	
	public int newZoneId;

	public ClusterRecord(String stationName, String lng, String lat, String passengerFlowAll, String timeFlow, String classId )
	{
		this.stationName = stationName;
		this.lngLat = new LngLat(lng, lat);
		this.passengerFlowAll = Integer.parseInt( passengerFlowAll );
		this.timeFlow = timeFlow;
		this.classId = Integer.parseInt( classId );
		this.realZoneId = -1;
		this.zoneId = -1;
		this.newZoneId = -1;
	}
	
	public ClusterRecord(String stationName, String lng, String lat, String passengerFlowAll, String timeFlow, String classId, String realZoneId, String zoneId )
	{
		this.stationName = stationName;
		this.lngLat = new LngLat(lng, lat);
		this.passengerFlowAll = Integer.parseInt( passengerFlowAll );
		this.timeFlow = timeFlow;
		this.classId = Integer.parseInt( classId );
		this.realZoneId = Integer.parseInt( realZoneId );
		this.zoneId = Integer.parseInt( zoneId );
		this.newZoneId = -1;
	}

	@Override
	public String toString()
	{
		return stationName + "," + lngLat.toString() + "," + passengerFlowAll + "," + timeFlow + "," + classId + "," + realZoneId + "," + zoneId + "," + newZoneId;
	}

	public String toFileString()
	{
		return stationName + "," + lngLat.toString() + "," + passengerFlowAll + "," + timeFlow + "," + classId + "," + realZoneId + "," + zoneId + "," + newZoneId;
	}
}