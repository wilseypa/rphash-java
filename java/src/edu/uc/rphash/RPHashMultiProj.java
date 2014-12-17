package edu.uc.rphash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

public class RPHashMultiProj {
	
	private List<float[]> centroids=null;
	List<float[]> data;
	int k;
	int d;
	int n;
	
	public RPHashMultiProj(List<float[]> data,int k){
		this.data = data;
		this.k =k;
		this.n = data.size();
		this.d = data.get(0).length;
	}


	

	public List<float[]> getCentroids(){
		
		if(centroids == null)run(data, k);
		return centroids;
	}
	
	private  void run(List<float[]> data, int k)
	{
		Random r = new Random();
//		List<float[]> prevCentroids = new ArrayList<float[]>();
//		float[] counts = new float[k];
		
		
		RPHash3Stage  rp ;
		
//		for(int j =0;j<k;j++)
//		{
//			float[] initCentroids = new float[d];
//			float[] centptr = rp.getCentroids().get(j);
//			//deep copy
//			for(int b = 0;b<k;b++)initCentroids[b] = centptr[b];
//			counts[j] = 1.0f;
//			prevCentroids.add(initCentroids);
//		}
		
		ArrayList<float[]> manyCentroids = new ArrayList<float[]> (10*k);
		for(int i=0;i<3 ;i++)
		{
			//System.out.println("\nscan:"+String.valueOf(i));
			rp = new RPHash3Stage (data,k,r.nextInt());
			manyCentroids.addAll(rp.getCentroids());
		}
		centroids =  ( new Kmeans(k,manyCentroids,d)).getCentroids();
	}
	
	public static GenerateData gen;
	public static void main(String[] args){
		
		int k = 20;
		int d = 10000;	
		int n = 10000;
		gen = new GenerateData(k,n/k,d,2.0f,true,1.f);
		RPHashMultiProj rphit = new RPHashMultiProj(gen.data(),k);
		
		long startTime = System.nanoTime();
		rphit.getCentroids();
		long duration = (System.nanoTime() - startTime);
		List<float[]> aligned  = TestUtil.alignCentroids(rphit.getCentroids(),gen.medoids());
		System.out.println(StatTests.PR(aligned,gen)+":"+duration/1000000000f);
		System.out.print("\n");
		System.gc();
		
	}
	
	
	
	
	
	
}
