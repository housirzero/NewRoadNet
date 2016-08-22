package kmeans;

import java.util.ArrayList;
import java.util.List;

public class Test {

	public static void main(String[] args) {
		List<Integer> list = new ArrayList<Integer>();
		List<Integer> centers = new ArrayList<Integer>();
		int i = 0;
		for(; i < 100; i++)
			list.add((int)(Math.random()*1000));
		int classNum = 10;
		for(i = 0; i < classNum; i++)
//			centers.add((int)(i*100));
			centers.add(i);
		
		KMeans<Integer> kmeans = new KMeans<Integer>(list, classNum, centers) {

			@Override
			public double distance(Integer e1, Integer e2) {
				return Math.abs(e1-e2);
			}

			@Override
			public Integer updateCenter(List<Integer> list) {
				if(list == null || list.size() == 0)
					return null;
				int res = 0;
				for(int i = 0; i < list.size(); i++)
					res += list.get(i);
				return res / list.size();
			}
		};
		
		kmeans.cluster();
		System.out.println("µü´ú´ÎÊı£º " + kmeans.getIterTimes());
		
		kmeans.display();
	}

}
