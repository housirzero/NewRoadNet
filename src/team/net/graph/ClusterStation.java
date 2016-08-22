package team.net.graph;

import java.util.ArrayList;

public class ClusterStation {
	// �������
	public int classId;
	// Сվ�б�
	public ArrayList<Station> stations;
	// ����
	public LngLat center;
	// �˴�վ������·�����ۺ�վ���·��
	public ArrayList<ClusterRoad> clusterRoads;

	public ClusterStation(int classId) {
		this.classId = classId;
		this.stations = new ArrayList<Station>();
		this.clusterRoads = new ArrayList<ClusterRoad>();
	}

	/**
	 * �ж�����ΪclassId��ClusterRoad�Ƿ����
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
	 * ��ȡ����ΪclassId��ClusterRoad
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
	 * ����˾������������<br>
	 * �ڴ��������վ�㶼�����ɺ��ټ���
	 */
	public void calCenter()
	{
		int n = stations.size();
		if( n == 0 )
		{
			System.err.println("�����վ����Ϊ0����������վ�������ɺ��ټ������ģ�");
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
