package kmeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import team.support.function.Distance;

/**
 * KMeans�㷨�Ļ���˼���ǳ�ʼ�������K�������ģ��������ڽ�ԭ��Ѵ�����������ֵ������ء�
 * Ȼ��ƽ�������¼�������ص����ģ��Ӷ�ȷ���µĴ��ġ�һֱ������ֱ�����ĵ��ƶ�����С��ĳ��������ֵ��
 * ����ʼ����ѡ�в���ʱ��KMeans�Ľ����ܲ����һ���Ƕ����м��Σ�����һ����׼��������ڵķ�����С����ѡ��һ���ȽϺõĽ����
 * <br>
 * <br>
 * 20150624 �� Ŀǰȱ�ٶ���Ⱥ��ļ�⣬Ӧ������һ����ֵ
 */
public abstract class KMeans<E> {

	/**
	 * Ҫ����ĵ�
	 */
	public List<ClusterPoint<E>> points = null;
	/**
	 * ��������
	 */
	public List<E> centers = null;
	/**
	 * Ҫ���ֵ������
	 */
	private int classNum;
	/**
	 * ��������
	 */
	private int iterTimes;
	
	private double maxDis; // �����룬�����˾Ͳ��ܻ��ֽ�һ����

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
	 * @param list : Ҫ����ĵ�
	 * @param classNum : Ҫ���ֵ������
	 */
	public KMeans(List<E> list, int classNum){
		if(classNum >= list.size()) // Ҫ������������С�������������൱��ÿ���������Գ�һ�ɣ�����Ҫ����
			System.out.println("Ҫ������������С����������,����Ҫ����!");

		iterTimes = 0;
		points = new ArrayList<ClusterPoint<E>>();
		for(int i = 0; i < list.size(); i++)
			points.add(new ClusterPoint<E>(list.get(i)));
		this.classNum = classNum;
	}

	/**
	 * 
	 * @param list : Ҫ����ĵ�
	 * @param classNum : Ҫ���ֵ������
	 * @param centers : ��������
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
	 * ��������ľ�����㷽��
	 * @param e1
	 * @param e2
	 * @return
	 */
	public abstract double distance(E e1, E e2);
	
	/**
	 * ����
	 */
	public void cluster(){
		if(centers == null)
		{
			// ���ȷ����ʼ��������,�������������ѡȡ
			centers = new ArrayList<E>();
			Set<Integer> centerIndexSet = new HashSet<Integer>();
			do {
				centerIndexSet.add((int)(Math.random()*points.size()));
			} while (centerIndexSet.size() < classNum);
			
			for(Integer index : centerIndexSet){
				centers.add(points.get(index).getE());
//				System.out.println(points.get(index).getE()); // ��ӡ���ѡ��ĳ�ʼ��������
			}
		}
		
		// ��ʼ����
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
					change = true; // �����������˱仯����δ����
				}
			}
			updateCenters();
			iterTimes++;
		} while (change);
	}
	
	/**
	 * �������еľ�������
	 * 20150624 �� �� map ������Ч��
	 */
	private void updateCenters() {
		// MAP<classId, List<���ڴ�classId�����е�>>
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
				centers.set(classId, newCenter); // ������� i �ľ�������
		}
	}
	
	/**
	 * ����ĳһ�������Ԫ�ظ��¾�������
	 */
	public abstract E updateCenter(List<E> list);

	/**
	 * �ҵ�����ĳһ����������ľ�����������
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
	 * ���ؾ�����
	 * @return
	 */
	public int[] getClusterResult(){
		int[] res = new int[points.size()];
		for(int i = 0; i < points.size(); i++)
			res[i] = points.get(i).getClassId();
		return res;
	}
	
	/**
	 * ��ʾ������
	 */
	public void display(){
		// MAP<classId, List<���ڴ�classId�����е�>>
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
			System.out.println("********************�� " + classId + " ��********************");
			if(list != null){
				for(int i = 0; i < list.size(); i++)
					System.out.println(list.get(i));
			}
		}
	}

}
