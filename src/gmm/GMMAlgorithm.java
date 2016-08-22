package gmm;

import java.util.List;
import java.util.ArrayList;

/**
 * GMM�㷢���裺<br>
 * 1. ��ʼ������������Gauss�ֲ���������ֵ��Э���<br>
 * 2. ����ÿ���ڵ�����ÿ���ֲ��ĸ��ʣ�<br>
 * 3. ����ÿ���ֲ�����ÿ���ڵ�ĸ��ʣ�<br>
 * 4. ����ÿ���ֲ���Ȩֵ����ֵ�����ǵ�Э���<br>
 */
public class GMMAlgorithm {
	
	/**
	 * 
	* @Title: GMMCluster 
	* @Description: GMM�����㷨��ʵ���࣬����ÿ�����ݵ����(0~k-1)
	* @return int[]
	* @throws
	 */
	public int[] GMMCluster(ArrayList<ArrayList<Double>> dataSet, ArrayList<ArrayList<Double>> pMiu, int dataNum, int k, int dataDimen) {
		Parameter parameter = iniParameters(dataSet, dataNum, k, dataDimen);
		double Lpre = -1000000; // ��һ�ξ�������
		double threshold = 0.0001;
		while(true) {
			ArrayList<ArrayList<Double>> px = computeProbablity(dataSet, pMiu, dataNum, k, dataDimen);
			double[][] pGama = new double[dataNum][k];
			for(int i = 0; i < dataNum; i++) {
				for(int j = 0; j < k; j++) {
					pGama[i][j] = px.get(i).get(j) * parameter.getpPi().get(j);
				}
			}
			
			double[] sumPGama = GMMUtil.matrixSum(pGama, 2);
			for(int i = 0; i < dataNum; i++) {
				for(int j = 0; j < k; j++) {
					pGama[i][j] = pGama[i][j] / sumPGama[i];
				}
			}
			
			double[] NK = GMMUtil.matrixSum(pGama, 1); // ��k����˹����ÿ�������ĸ��ʵĺͣ�����Nk���ܺ�ΪN
			
			// ����pMiu
			double[] NKReciprocal = new double[NK.length];
			for(int i = 0; i < NK.length; i++) {
				NKReciprocal[i] = 1 / NK[i];
			}
			double[][] pMiuTmp = GMMUtil.matrixMultiply(GMMUtil.matrixMultiply(GMMUtil.diag(NKReciprocal), GMMUtil.matrixReverse(pGama)), GMMUtil.toArray(dataSet));
			
			// ����pPie
			double[][] pPie = new double[k][1];
			for(int i = 0; i < NK.length; i++) {
				pPie[i][0] = NK[i] / dataNum;
			}
			
			// ����k��pSigma
			double[][][] pSigmaTmp = new double[dataDimen][dataDimen][k];
			for(int i = 0; i < k; i++) {
				double[][] xShift = new double[dataNum][dataDimen];
				for(int j = 0; j < dataNum; j++) {
					for(int l = 0; l < dataDimen; l++) {
						xShift[j][l] = pMiuTmp[i][l];
					}
				}
				
				double[] pGamaK = new double[dataNum]; // ��k��pGamaֵ
				for(int j = 0; j < dataNum; j++) {
					pGamaK[j] = pGama[j][i];
				}
				double[][] diagPGamaK = GMMUtil.diag(pGamaK);
				
				double[][] pSigmaK = GMMUtil.matrixMultiply(GMMUtil.matrixReverse(xShift), (GMMUtil.matrixMultiply(diagPGamaK, xShift)));
				for(int j = 0; j < dataDimen; j++) {
					for(int l = 0; l < dataDimen; l++) {
						pSigmaTmp[j][l][k] = pSigmaK[j][l] / NK[i];
					}
				}
			}
			
			// �ж��Ƿ��������
			double[][] a = GMMUtil.matrixMultiply(GMMUtil.toArray(px), pPie);
			for(int i = 0; i < dataNum; i++) {
				a[i][0] = Math.log(a[i][0]);
			}
			double L = GMMUtil.matrixSum(a, 1)[0];
			
			if(L - Lpre < threshold) {
				break;
			}
			Lpre = L;
		}
		return null;
	}
	
	/**
	 * 
	* @Title: computeProbablity 
	* @Description: ����ÿ���ڵ㣨��n��������ÿ���ֲ���k�����ĸ���
	* @return ArrayList<ArrayList<Double>>
	* @throws
	 */
	public ArrayList<ArrayList<Double>> computeProbablity(ArrayList<ArrayList<Double>> dataSet, ArrayList<ArrayList<Double>> pMiu, int dataNum, int k, int dataDimen) {
		double[][] px = new double[dataNum][k]; // ÿ����������ÿ���ֲ��ĸ��� 
		int[] type = getTypes(dataSet, pMiu, k, dataNum);
		
		// ����k���ֲ���Э�������
		ArrayList<ArrayList<ArrayList<Double>>> covList = new ArrayList<ArrayList<ArrayList<Double>>>();
		for(int i = 0; i < k; i++) {
			ArrayList<ArrayList<Double>> dataSetK = new ArrayList<ArrayList<Double>>();
			for(int j = 0; j < dataNum; j++) {
				if(type[k] == i) {
					dataSetK.add(dataSet.get(i));
				}
			}
//			covList.set(i, GMMUtil.computeCov(dataSetK, dataDimen, dataSetK.size()));
			covList.add(GMMUtil.computeCov(dataSetK, dataDimen, dataSetK.size()));
		}
		
		// ����ÿ����������ÿ���ֲ��ĸ��� 
		for(int i = 0; i < dataNum; i++) {
			for(int j = 0; j < k; j++) {
				ArrayList<Double> offset = GMMUtil.matrixMinus(dataSet.get(i), pMiu.get(j));
				ArrayList<ArrayList<Double>> invSigma = covList.get(j);
				double[] tmp = GMMUtil.matrixSum(GMMUtil.matrixMultiply(GMMUtil.toArray1(offset), GMMUtil.toArray(invSigma)), 2);
				double coef = Math.pow((2 * Math.PI), -(double)dataDimen / 2d) * Math.sqrt(GMMUtil.computeDet(invSigma, invSigma.size()));
				px[i][j] = coef * Math.pow(Math.E, -0.5 * tmp[0]);
			}
		}
		
		return GMMUtil.toList(px);
	}
	
	/**
	 * 
	* @Title: iniParameters 
	* @Description: ��ʼ������Parameter
	* @return Parameter
	* @throws
	 */
	public Parameter iniParameters(ArrayList<ArrayList<Double>> dataSet, int dataNum, int k, int dataDimen) {
		Parameter res = new Parameter();
		
		ArrayList<ArrayList<Double>> pMiu = generateCentroids(dataSet, dataNum, k);
		res.setpMiu(pMiu);
		
		// ����ÿ�������ڵ���ÿ�����Ľڵ�ľ��룬�Դ�Ϊ�ݶ������ڵ���з��������������ʼ��k���ֲ���Ȩֵ
		ArrayList<Double> pPi = new ArrayList<Double>();
		int[] type = getTypes(dataSet, pMiu, k, dataNum);
		int[] typeNum = new int[k];
		for(int i = 0; i < dataNum; i++) {
			typeNum[type[i]]++;
		}
		for(int i = 0; i < k; i++) {
			pPi.add((double)(typeNum[i]) / (double)(dataNum));
		}
		res.setpPi(pPi);
		
		// ����k���ֲ���k��Э����
		ArrayList<ArrayList<ArrayList<Double>>> pSigma = new ArrayList<ArrayList<ArrayList<Double>>>();
		for(int i = 0; i < k; i++) {
			ArrayList<ArrayList<Double>> tmp = new ArrayList<ArrayList<Double>>();
			for(int j = 0; j < dataNum; j++) {
				if(type[j] == i) {
					tmp.add(dataSet.get(i));
				}
			}
			pSigma.add(GMMUtil.computeCov(tmp, dataDimen, dataNum));
		}
		res.setpSigma(pSigma);
		
		return res;
	}
	
	/**
	 * 
	* @Title: generateCentroids 
	* @Description: ��ȡ�����k�����ĵ�
	* @return ArrayList<ArrayList<Double>>
	* @throws
	 */
	public ArrayList<ArrayList<Double>> generateCentroids(ArrayList<ArrayList<Double>> data, int dataNum, int k) {
		ArrayList<ArrayList<Double>> res = null;
		if(dataNum < k) {
			return res;
		} else {
			res =  new ArrayList<ArrayList<Double>>();
			
			List<Integer> random = new ArrayList<Integer>();
			// ����������ظ���k����
			while(k > 0) {
				int index = (int)(Math.random() * dataNum);
			    if(!random.contains(index)) {
			    	random.add(index);
			    	k--;
			    	res.add(data.get(index));
			    }
			}
		}
		return res;
	}
	
	/**
	 * @Title: getTypes 
	 * @Description: ����ÿ�����ݵ����
	 * @return int[]
	 * @throws
	 */
	public int[] getTypes(ArrayList<ArrayList<Double>> dataSet, ArrayList<ArrayList<Double>> pMiu, int k, int dataNum) {
		int[] type = new int[dataNum];
		for(int j = 0; j < dataNum; j++) {
			double minDistance = GMMUtil.computeDistance(dataSet.get(j), pMiu.get(0));
			type[j] = 0; // 0��Ϊ�������ݵ����
			for(int i = 1; i < k; i++) {
				if(GMMUtil.computeDistance(dataSet.get(j), pMiu.get(0)) < minDistance) {
					minDistance = GMMUtil.computeDistance(dataSet.get(j), pMiu.get(0));
					type[j] = k;
				}
			}
		}
		return type;
	}
	
	public static void main(String[] args) {
		ArrayList<Double> pPi = new ArrayList<Double>();
		System.out.println(pPi.get(0));
		
		GMMAlgorithm gmm = new GMMAlgorithm();
		ArrayList<ArrayList<Double>> dataSet = null; // ���ݼ�
		ArrayList<ArrayList<Double>> pMiu = null; // ��ֵ����k���ֲ������ĵ㣬ÿ�����ĵ�dά
		int dataNum = 0; // ������������ dataSet.size()
		int k = 0; // ������
		int dataDimen = 0; // ά�ȣ������Ƕ�ά���꣬������2
		// 1. ��ʼ������������Gauss�ֲ���������ֵ��Э����
		// 2. ����ÿ���ڵ�����ÿ���ֲ��ĸ���
		// 3. ����ÿ���ֲ�����ÿ���ڵ�ĸ���
		// 4. ����ÿ���ֲ���Ȩֵ����ֵ�����ǵ�Э����
		int[] classId = gmm.GMMCluster(dataSet, pMiu, dataNum, k, dataDimen);
	}
}