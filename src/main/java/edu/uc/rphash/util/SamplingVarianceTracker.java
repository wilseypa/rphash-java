package edu.uc.rphash.util;

import java.util.Random;

public class SamplingVarianceTracker {
	private float n;
	private float[] mean;
	private float[] M2 ;
	private int sampRatio;
	private long randomness;
	private int count=0;
	
	public SamplingVarianceTracker(){
		M2 = null;
		mean = null;
		n = 0;
		sampRatio = 1;
		randomness = new Random().nextLong();
	}
	
	public SamplingVarianceTracker(int sampRatio){
		M2 = null;
		mean = null;
		n = 0;
		this.sampRatio = sampRatio/2;
		randomness = new Random().nextLong();
	}

	/** Get the current sampled Variance 
	 * @return
	 */
	public float[] getVariance(){
		float[] ret = new float[M2.length];
		for(int i = 0;i<M2.length;i++){
			ret[i] = (float) Math.sqrt(M2[i] /(float)(n-1));
		}
		return ret;
	}
	
	public float[] normailize(float[] in){
		VectorUtil.prettyPrint(in);
		System.out.println();
		n++;
		if(M2==null){
			M2 = new float[in.length];
			mean = new float[in.length];
		}
		
		float[] ret = new float[M2.length];
		for(int i = 0;i<M2.length;i++){
			float delta =in[i]- mean[i];
			mean[i] = mean[i] + delta/n;
			M2[i] = M2[i] + delta*(in[i]-mean[i]);
			if(n-1>0)
				ret[i] = (float)(in[i]/ (Math.sqrt(M2[i] / (float)(n-1))));
			else
				ret[i] = (float)(in[i]);
		}
		return ret;
	}
	
	/** Update the variance with probability sampRatio
	 * @param row
	 * @return whether variance was updated or not
	 */
	public boolean updateVarianceSample(float[] row)
	{
		count++;
		//prime the variance computation
		if(count < 1000)
		{
			n++;
			if(M2==null){
				M2 = new float[row.length];
				mean = new float[row.length];
			}
			for(int i = 0;i<row.length;i++){
				float delta =row[i]- mean[i];
				mean[i] = mean[i] + delta/n;
				M2[i] = M2[i] + delta*(row[i]-mean[i]);
			}	
			return true;
		}
		
		//skip ratio data, use a random 64 bit cycle twist to increase randomness
		if((count % sampRatio)==0 && ((randomness>>>((count)%64) )&1)==0)
		{
			n++;
			for(int i = 0;i<row.length;i++){
				float delta =row[i]- mean[i];
				mean[i] = mean[i] + delta/n;
				M2[i] = M2[i] + delta*(row[i]-mean[i]);
			}	
			return  true;
		}
		return false;
	}
	
	public static void main(String[] args){
		
		SamplingVarianceTracker vartrack = new	SamplingVarianceTracker(1000);
		
		Random r = new Random();
		int d = 10;
		int n = 1000000;
		
		for(int i = 0;i<n;i++){
			float[] vec = new float[d];
			for(int j = 0 ;j <d;j++){
				vec[j]= (float) (r.nextGaussian()*(j));
			}
			vartrack.updateVarianceSample(vec);
		}
		System.out.println(vartrack.n-1000 + ":"+n/1000);
		VectorUtil.prettyPrint(vartrack.getVariance());
		
	}

}
