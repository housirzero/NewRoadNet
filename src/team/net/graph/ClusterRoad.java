package team.net.graph;

public class ClusterRoad {
	
//	// ��ʼ����վ������
//	public int startStationId;
	// Ŀ�ľ���վ������
	public int endClustStatId;
	// ��·���ͣ��Ƿ������������������ٹ���
	public int kind;
	public ClusterRoad(int endClustStatId) {
		this.endClustStatId = endClustStatId;
		this.kind = 0;
	}
}
