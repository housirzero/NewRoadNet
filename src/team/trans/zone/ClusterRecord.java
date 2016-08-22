package team.trans.zone;

import team.net.graph.LngLat;

/**
 * δʹ�ý�ͨС���ľ�����
 * @author DELL
 *
 */
public class ClusterRecord
{
	public String stationName;
	public LngLat lngLat;
	// ����������int�ͣ������ڲ����õ����������ַ�����¼����ʡ������Դ
	public int passengerFlowAll;
	public String timeFlow;
	/**
	 * �����
	 */
	public int classId;
	/**
	 * ������ͨС������
	 */
	public int realZoneId;
	
	/**
	 * �������Ӷ������ZoneId
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