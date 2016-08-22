package team.net.graph;

public class ClusterRoad {
	
//	// 起始聚类站点的类号
//	public int startStationId;
	// 目的聚类站点的类号
	public int endClustStatId;
	// 线路类型，是否包含地铁、公交或快速公交
	public int kind;
	public ClusterRoad(int endClustStatId) {
		this.endClustStatId = endClustStatId;
		this.kind = 0;
	}
}
