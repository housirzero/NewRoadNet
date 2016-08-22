package team.net.graph;

public class Station {
	public String name;
	public String lineId;
	// 站点编号
	public int stationNo;
	public LngLat pos;
	// 线路类型，有公交、地铁以及快速公交
	// 1 公交 2 快速公交 4 地铁(这样表示，分别代表不同的二进制位，便于进行或运算)
	public int kind;
	// 上一站，一条线路上只可能有一个上一站
	public Station lastStation;
	// 下一站
	public Station nextStation;
	// 所属聚类类别
	public int classId;
	// 所属交通小区
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
