package edu.uc.rphash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

public class RPHashMultiRP {

	private List<float[]> centroids=null;
	List<float[]> data;
	int k;
	int d;
	int n;
	
	public RPHashMultiRP(List<float[]> data,int k){
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
			
		RPHashSimple  rp ;
		
//		for(int j =0;j<k;j++)
//		{
//			float[] initCentroids = new float[d];
//			float[] centptr = rp.getCentroids().get(j);
//			//deep copy
//			for(int b = 0;b<k;b++)initCentroids[b] = centptr[b];
//			counts[j] = 1.0f;
//			prevCentroids.add(initCentroids);
//		}
		
		ArrayList<float[]> manyCentroids = new ArrayList<float[]> (k);
		for(int i=0;i<10 ;i++)
		{
			rp = new RPHashSimple (data,k);
			manyCentroids.addAll(rp.getCentroids());
		}
		centroids =  ( new Kmeans(k,manyCentroids,0)).getCentroids();
	}
	
	public static GenerateData gen;
	public static void main(String[] args){
		
		int k = 20;
		int d = 5000;	
		int n = 10000;
		gen = new GenerateData(k,n/k,d,1.0f,true,1.f);
		RPHashMultiRP rphit = new RPHashMultiRP(gen.data(),k);
		
		long startTime = System.nanoTime();
		rphit.getCentroids();
		long duration = (System.nanoTime() - startTime);
		List<float[]> aligned  = TestUtil.alignCentroids(rphit.getCentroids(),gen.medoids());
		System.out.println(StatTests.PR(aligned,gen)+":"+duration/1000000000f);
		System.out.print("\n");
		System.gc();
		
	}
	
	
	
	
	
	
}
