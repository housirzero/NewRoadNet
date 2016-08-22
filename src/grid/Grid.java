package grid;

import java.util.List;
import java.util.Date;
import team.net.graph.LngLat;

public class Grid {
	
	private List<LngLat> centers; // 聚类中心
	private LineSegment lineSeg; // 线段
	
	public static void main(String[] args) {
		Date start = new Date();
		run();
		Date end = new Date();
		System.out.println("run time : " + (end.getTime() - start.getTime())
				/ 1000.0 + " s.");
	}

	public static void run() {

		// kmeans 聚类结果作为初始
		// 找到所有站点的最小凸闭包
		// 在相近的两个类的核心点之间画中垂线，并结合最外层的凸闭包，将区域划分出来
		// 形成每个区的信息记录（边界点序列）
		// 在百度地图上投影
	}
}
