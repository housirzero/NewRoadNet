package team.net.graph;

import java.util.ArrayList;

public class ClusterStation {
	// 聚类类别
	public int classId;
	// 小站列表
	public ArrayList<Station> stations;
	// 质心
	public LngLat center;
	// 此大站的所有路径（聚合站间的路）
	public ArrayList<ClusterRoad> clusterRoads;

	public ClusterStation(int classId) {
		this.classId = classId;
		this.stations = new ArrayList<Station>();
		this.clusterRoads = new ArrayList<ClusterRoad>();
	}

	/**
	 * 判断类别号为classId的ClusterRoad是否存在
	 */
	public boolean containsClusterStation( int classId )
	{
		for(ClusterRoad clusterRoad : clusterRoads)
		{
			if(clusterRoad.endClustStatId == classId)
				return true;
		}
		return false;
	}
	
	/**
	 * 获取类别号为classId的ClusterRoad
	 */
	public ClusterRoad getClusterStation( int classId )
	{
		for(ClusterRoad clusterRoad : clusterRoads)
		{
			if(clusterRoad.endClustStatId == classId)
				return clusterRoad;
		}
		return null;
	}
	
	/**
	 * 计算此聚类区域的质心<br>
	 * 在此类别所有站点都添加完成后再计算
	 */
	public void calCenter()
	{
		int n = stations.size();
		if( n == 0 )
		{
			System.err.println("此类别站点数为0，请在所有站点添加完成后再计算质心！");
			return ;
		}
		double lng = 0.0;
		double lat = 0.0;
		for(Station station : stations)
		{
			lng += station.pos.lng;
			lat += station.pos.lat;
		}
		lng = lng / n;
		lat = lat / n;
		center = new LngLat(lng, lat);
	}
	
}
