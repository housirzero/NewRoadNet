package kmeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import team.support.function.Distance;

/**
 * KMeans算法的基本思想是初始随机给定K个簇中心，按照最邻近原则把待分类样本点分到各个簇。
 * 然后按平均法重新计算各个簇的质心，从而确定新的簇心。一直迭代，直到簇心的移动距离小于某个给定的值。
 * 当初始簇心选行不好时，KMeans的结果会很差，所以一般是多运行几次，按照一定标准（比如簇内的方差最小化）选择一个比较好的结果。
 * <br>
 * <br>
 * 20150624 ： 目前缺少对离群点的检测，应该设置一个阈值
 */
public abstract class KMeans<E> {

	/**
	 * 要聚类的点
	 */
	public List<ClusterPoint<E>> points = null;
	/**
	 * 聚类中心
	 */
	public List<E> centers = null;
	/**
	 * 要划分的类别数
	 */
	private int classNum;
	/**
	 * 迭代次数
	 */
	private int iterTimes;
	
	private double maxDis; // 最大距离，超过了就不能划分进一类中

	public double getMaxDis() {
		return maxDis;
	}

	public void setMaxDis(double maxDis) {
		this.maxDis = maxDis;
	}

	public int getIterTimes() {
		return iterTimes;
	}

	public KMeans(){
		points = new ArrayList<ClusterPoint<E>>();
	}
	
	/**
	 * @param list : 要聚类的点
	 * @param classNum : 要划分的类别数
	 */
	public KMeans(List<E> list, int classNum){
		if(classNum >= list.size()) // 要聚类的类别数不小于样本点数，相当于每个样本点自成一派，不需要聚类
			System.out.println("要聚类的类别数不小于样本点数,不需要聚类!");

		iterTimes = 0;
		points = new ArrayList<ClusterPoint<E>>();
		for(int i = 0; i < list.size(); i++)
			points.add(new ClusterPoint<E>(list.get(i)));
		this.classNum = classNum;
	}

	/**
	 * 
	 * @param list : 要聚类的点
	 * @param classNum : 要划分的类别数
	 * @param centers : 聚类中心
	 */
	public KMeans(List<E> list, int classNum, List<E> centers){
		this(list, classNum);
		this.centers = centers;
	}
	
	public void add(E e){
		points.add(new ClusterPoint<E>(e));
	}
	
	public void clear(){
		points = null;
	}
	
	public int size(){
		return points.size();
	}
	
	/**
	 * 两样本点的距离计算方法
	 * @param e1
	 * @param e2
	 * @return
	 */
	public abstract double distance(E e1, E e2);
	
	/**
	 * 聚类
	 */
	public void cluster(){
		if(centers == null)
		{
			// 随机确定初始聚类中心,从样本点中随机选取
			centers = new ArrayList<E>();
			Set<Integer> centerIndexSet = new HashSet<Integer>();
			do {
				centerIndexSet.add((int)(Math.random()*points.size()));
			} while (centerIndexSet.size() < classNum);
			
			for(Integer index : centerIndexSet){
				centers.add(points.get(index).getE());
//				System.out.println(points.get(index).getE()); // 打印随机选择的初始聚类中心
			}
		}
		
		// 开始聚类
		boolean change = false;
		iterTimes = 0;
		do {
			change = false;
			for(int i = 0; i < points.size(); i++){
				ClusterPoint<E> point = points.get(i);
				int classId = getClassIndex(point.getE());
				if(distance(point.getE(), centers.get(classId)) > this.maxDis)
						continue;
				if(classId != point.getClassId()){
					point.setClassId(classId);
					change = true; // 聚类结果发生了变化，尚未收敛
				}
			}
			updateCenters();
			iterTimes++;
		} while (change);
	}
	
	/**
	 * 更新所有的聚类中心
	 * 20150624 ： 用 map 来提升效率
	 */
	private void updateCenters() {
		// MAP<classId, List<属于此classId的所有点>>
		Map<Integer, List<E>> classIdIndexMap = new HashMap<Integer, List<E>>();
		for(int i = 0; i < centers.size(); i++)
			classIdIndexMap.put(i, new ArrayList<E>());
		
		for(int j = 0; j < points.size(); j++){
			ClusterPoint<E> point = points.get(j);
			if(point.getClassId() != -1)
				classIdIndexMap.get(point.getClassId()).add(point.getE());
		}
		
		for(Integer classId : classIdIndexMap.keySet())
		{
			E newCenter = updateCenter(classIdIndexMap.get(classId));
			if(newCenter != null)
				centers.set(classId, newCenter); // 更新类别 i 的聚类中心
		}
	}
	
	/**
	 * 根据某一类的所有元素更新聚类中心
	 */
	public abstract E updateCenter(List<E> list);

	/**
	 * 找到距离某一样本点最近的聚类中心索引
	 * @return
	 */
	private int getClassIndex(E e){
		double minDis = distance(e, centers.get(0));
		int minIndex = 0;
		for(int i = 1; i < centers.size(); i++){
			double dis = distance(e, centers.get(i));
			if(minDis > dis)
			{
				minDis = dis;
				minIndex = i;
			}
		}
		return minIndex;
	}
	
	/**
	 * 返回聚类结果
	 * @return
	 */
	public int[] getClusterResult(){
		int[] res = new int[points.size()];
		for(int i = 0; i < points.size(); i++)
			res[i] = points.get(i).getClassId();
		return res;
	}
	
	/**
	 * 显示分类结果
	 */
	public void display(){
		// MAP<classId, List<属于此classId的所有点>>
		Map<Integer, List<E>> classIdIndexMap = new HashMap<Integer, List<E>>();
		for(int i = 0; i < centers.size(); i++)
			classIdIndexMap.put(i, new ArrayList<E>());
		
		for(int j = 0; j < points.size(); j++){
			ClusterPoint<E> point = points.get(j);
			if(point.getClassId() != -1)
				classIdIndexMap.get(point.getClassId()).add(point.getE());
		}
		
		for(int classId = 0; classId < classNum; classId++)
		{
			List<E> list = classIdIndexMap.get(classId);
			System.out.println("********************第 " + classId + " 类********************");
			if(list != null){
				for(int i = 0; i < list.size(); i++)
					System.out.println(list.get(i));
			}
		}
	}

}
