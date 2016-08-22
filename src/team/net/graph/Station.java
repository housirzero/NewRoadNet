package team.net.graph;

public class Station {
	public String name;
	public String lineId;
	// վ����
	public int stationNo;
	public LngLat pos;
	// ��·���ͣ��й����������Լ����ٹ���
	// 1 ���� 2 ���ٹ��� 4 ����(������ʾ���ֱ����ͬ�Ķ�����λ�����ڽ��л�����)
	public int kind;
	// ��һվ��һ����·��ֻ������һ����һվ
	public Station lastStation;
	// ��һվ
	public Station nextStation;
	// �����������
	public int classId;
	// ������ͨС��
	public int zoneId;

	public Station(String name, String lineId, int stationNo, LngLat pos,
			int kind, Station lastStation, Station nextStation, int classId,
			int zoneId) {
		super();
		this.name = name;
		this.lineId = lineId;
		this.stationNo = stationNo;
		this.pos = pos;
		this.kind = kind;
		this.lastStation = lastStation;
		this.nextStation = nextStation;
		this.classId = classId;
		this.zoneId = zoneId;
	}

	@Override
	public String toString() {
		return "name=" + name + ", lineId=" + lineId + ", stationNo=" + stationNo + ", classId=" + classId + "\r\n" + nextStation;
	}
	
}
