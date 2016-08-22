package team.od.taxi;

import team.net.graph.LngLat;

public class TaxiOD {

	public String taxiID;
	
	public LngLat startPoint;
	public String startTime;
	public int startZone;
	
	public LngLat endPoint;
	public String endTime;
	public int endZone;
	
	public int pathTime;
	
	public TaxiOD(String taxiID,LngLat startPoint,String startTime,int startZone,LngLat endPoint,String endTime,int endZone,int pathTime)
	{
		this.taxiID = taxiID;
		this.startPoint = startPoint;
		this.startTime = startTime;
		this.startZone = startZone;
		this.endPoint = endPoint;
		this.endTime = endTime;
		this.endZone = endZone;
		this.pathTime = pathTime;
	}
}
