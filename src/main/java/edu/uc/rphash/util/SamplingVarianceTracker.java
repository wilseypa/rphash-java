package edu.uc.rphash.util;

import java.util.ArrayList;

import edu.uc.rphash.tests.StatTests;


public class SamplingVarianceTracker {
	private float n;
	private float mean;
	private float M2 ;
	private int sampRatio;
	private int samplingcounter;
	public SamplingVarianceTracker(){
		M2 = 0;
		mean = 0;
		n = 0;
		sampRatio = 1;
		samplingcounter = 0;
	}
	private ArrayList<float[]> alldata = new ArrayList<>();
	
	
	
	public float updateVarianceSample(float[] row)
	{
		
//		if(++samplingcounter%sampRatio==0)return M2/(n-1f);

		for(float x : row){
			n++;
			float delta = x - mean;
			mean = mean + delta/n;
			M2 = M2 + delta*(x-mean);
		}	
		if(n<2)return .1f;
		return  16;//M2/(n-1f);
	}

}
