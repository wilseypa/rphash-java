package edu.uc.rphash.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Readers.StreamObject;

public class StatTests {
	Random r;
	double sampRatio;
	public StatTests(double sampRatio) {
		r = new Random();
		this.sampRatio = sampRatio;
	}


	public static float PR(List<float[]> estCentroids, ClusterGenerator gen){
		int count = 0 ;
		List<float[]> data = gen.getData();
		for(int i = 0; i< data.size();i++)
		{
			if(TestUtil.findNearestDistance(data.get(i), estCentroids)==gen.getLabels().get(i))count++;
		}
		System.out.println(data.size());
		return (float)count/(float)data.size();
	}
	
	
	public static double WCSSE(List<float[]> estCentroids, List<float[]> data){
		double count = 0.0 ;
		for(int i = 0; i< data.size();i++)
		{
			count+=TestUtil.distance(data.get(i),estCentroids.get(TestUtil.findNearestDistance(data.get(i), estCentroids))) ;
		}
		return count;
	}
	
	public static double WCSSE(List<float[]> estCentroids, String f,boolean raw) throws IOException{
		double count = 0.0 ;
		StreamObject data = new StreamObject(f,0,raw);
		while(data.hasNext())
		{
			float[] next = data.next();
			count+=TestUtil.distance(next,estCentroids.get(TestUtil.findNearestDistance(next, estCentroids))) ;
		}
		return count;
	}

	public static double SSE(List<float[]> estCentroids, ClusterGenerator gen){
		double count = 0.0 ;
		List<float[]> data = gen.getMedoids();
		for(int i = 0; i< data.size();i++)
		{
			count+=TestUtil.distance(data.get(i),estCentroids.get(TestUtil.findNearestDistance(data.get(i), estCentroids))) ;
		}
		return count;
	}
	
	private float n = 0;
	private float mean = 0;
	private float M2 = 0;
	public float updateVarianceSample(float[] row){
		
		if(r.nextFloat()>sampRatio)return M2/(n-1f);
		
		for(float x : row){
			n++;
			float delta = x - mean;
			mean = mean + delta/n;
			M2 = M2 + delta*(x-mean);
		}	
		if(n<2)return 0;
		return  M2/(n-1f);
	}
	
	
	
	public static float varianceSample(List<float[]> data,float sampRatio){
		float n = 0;
		float mean = 0;
		float M2 = 0;
		Random r = new Random();
		
		int len = data.size();
		
		for(int i = 0 ; i<sampRatio*len; i++){
			float[] row = data.get(r.nextInt(len));
			
			for(float x : row){
				n++;
				float delta = x - mean;
				mean = mean + delta/n;
				M2 = M2 + delta*(x-mean);
			}	
		}
		if(n<2)return 0;
		
		return  M2/(n-1f);
	}
	
	
	public static float varianceAll(List<float[]> data){
		float n = 0;
		float mean = 0;
		float M2 = 0;

		for(float[] row : data){
			for(float x : row){
				n++;
				float delta = x - mean;
				mean = mean + delta/n;
				M2 = M2 + delta*(x-mean);
			}	
		}
		if(n<2)return 0;
		
		return  M2/(n-1f);
	}
	
	public static float averageAll(List<float[]> data){
		float n = 0;
		float mean = 0;
		for(float[] row : data){
			for(float x : row){
				n++;
				mean+=x;
			}	
		}return mean/n;
	}
	
	public static float[] varianceCol(List<float[]> data){
		if(data.size()<1)return null;
		float[] vars = new float[data.get(0).length];
		for(int i=0;i<data.size();i++ )
		{
			float n = 0;
			float mean = 0;
			float M2 = 0;
			
			for(float x : data.get(i)){
				n++;
				float delta = x - mean;
				mean = mean + delta/n;
				M2 = M2 + delta*(x-mean);
			}
			if(n<2)vars[i]=0;
			else vars[i] = M2/(n-1f);
		}
		return vars;
	}
	
	public static float[] averageCol(List<float[]> data){
		if(data.size()<1)return null;
		int n = data.size();
		int d = data.get(0).length;
		float[] avgs = new float[d];
		

		for(float[] tmp : data){

			for(int j=0;j<d;j++)
			{
				
				avgs[j]+=(tmp[j]/n);
			}	
			
		}
		return avgs;
	}


	public static double variance(double[] row) {
		double n = 0;
		double mean = 0;
		double M2 = 0;
		for(double x : row){
			n++;
			double delta = x - mean;
			mean = mean + delta/n;
			M2 = M2 + delta*(x-mean);
		}
		if(n<2)return 0;
		
		return  M2/(n-1f);
	}

}
