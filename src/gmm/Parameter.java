package gmm;

import java.util.ArrayList;

public class Parameter {  
    private ArrayList<ArrayList<Double>> pMiu; // ��ֵ����k���ֲ������ĵ㣬ÿ�����ĵ�dά  
    private ArrayList<Double> pPi; // k��GMM��Ȩֵ  
    private ArrayList<ArrayList<ArrayList<Double>>> pSigma; // k��GMM��Э�������,d*d*k  
      
    public ArrayList<ArrayList<Double>> getpMiu() {  
        return pMiu;  
    }  
    public void setpMiu(ArrayList<ArrayList<Double>> pMiu) {  
        this.pMiu = pMiu;  
    }  
    public ArrayList<Double> getpPi() {  
        return pPi;  
    }  
    public void setpPi(ArrayList<Double> pPi) {  
        this.pPi = pPi;  
    }  
    public ArrayList<ArrayList<ArrayList<Double>>> getpSigma() {  
        return pSigma;  
    }  
    public void setpSigma(ArrayList<ArrayList<ArrayList<Double>>> pSigma) {  
        this.pSigma = pSigma;  
    }  
}  
