package edu.uc.rphash.tests;

import java.util.ArrayList;
import java.util.List;

import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.GaussianProjection;
import edu.uc.rphash.projections.Projector;

public class Kmeans {

	
	

	int k;
	int n;
	
	public float[] computerCentroid(List<Integer> vectors,List<float[]> data ){
		int d = data.get(0).length;
		float[] centroid = new float[d];

		for(int i = 0 ; i<d;i++)
			centroid[i] = 0.0f;

		float scalr = 1.0f/(float)vectors.size();
		for(Integer v : vectors)
		{
			float[] vec = data.get(v);
			for(int i = 0 ; i<d;i++)
				centroid[i] += (vec[i]*scalr);	
		}
		
		return centroid;
	}
	
	void updateMeans(List<float[]> data )
	{
		for(int i = 0; i< k;i++)means.set(i,computerCentroid(clusters.get(i),data));
	}
	
	int assignClusters(List<float[]> data)
	{
		int swaps = 0 ;
		List<List<Integer>> newClusters = new ArrayList<List<Integer>>();
		for(int j=0;j<k;j++)newClusters.add(new ArrayList<Integer>());
		
		for(int clusterid = 0 ; clusterid< k;clusterid++)
		{
			for(Integer member: clusters.get(clusterid)){
				int nearest = TestUtil.findNearestDistance(data.get(member), means);
				newClusters.get(nearest).add(member);
				if(nearest!=clusterid)swaps++;
			}		
		}
		clusters = newClusters;
		return swaps;
	}
	
	
	
	List<float[]> means; 
	List<List<Integer>> clusters;
	public Kmeans(int k, List<float[]> data,int projdim)
	{
		run(k,data,projdim);
	} 
//	public Kmeans(int k, List<float[]> data)
//	{
//	
//		run(k,data,24);
//	}
//	
	public void run(int k, List<float[]> data,int projdim){
		int maxiters = 10000;
		int swaps = 3;
		List<float[]> fulldata = data;
		data = new ArrayList<float[]>();
		Projector p=null;
		if(projdim!=0)
			p =  new DBFriendlyProjection(fulldata.get(0).length,projdim, 19797);
		
		for(float[] v: fulldata){
			if(p!=null){
				data.add(p.project(v));
			}
			else
				data.add(v);
		}
		
		this.n = data.size();
		this.k = k;
		
		//initialize the clusters to the n/k step first vectors
		means = new ArrayList<float[]>(k);
		for(int i = 0 ; i< k;i++)means.add(data.get(i*(n/k)));
		
		//initialize each cluster gets n/k of the dataset at first
		clusters = new ArrayList<List<Integer>>(k);
		for(int i = 0 ; i< k;i++)
		{
			List<Integer> tmp =  new ArrayList<Integer>(n/k);
			int start = i*(n/k);
			for(int j = 0 ;j<n/k;j++)tmp.add(j+start);
			clusters.add(tmp);
		}
		while(swaps>2 && maxiters>0){
			maxiters--;
			updateMeans(data);
			swaps = assignClusters(data);
		}
		if(maxiters == 0)System.err.println("Warning: MaxIterations Reached");
		data = fulldata;
		updateMeans(data);
		
	}
	
	
	

	
	public List<float[]> getCentroids()
	{
		return means;
	}
	
	public static void main(String[] args){
		GenerateData gen = new GenerateData(8,100,100);
		Kmeans kk = new Kmeans(5,gen.data(),24);
		TestUtil.prettyPrint(kk.getCentroids());
	}


}
