package edu.uc.rphash.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Kmeans {
	
	int d;
	int k;
	int n;
	float[] computerCentroid(List<float[]> vectors ){
		float[] centroid = new float[d];
		for(int i = 0 ; i<d;i++)
			centroid[i] = 0.0f;

		float scalr = 1.0f/(float)vectors.size();
		for(float[] vec : vectors)
		{
			for(int i = 0 ; i<d;i++)
				centroid[i] += (vec[i]*scalr);	
		}
		return centroid;
	}
	
	void updateMeans()
	{
		for(int i = 0; i< k;i++)means.set(i,computerCentroid(clusters.get(i)));
	}
	
	int assignClusters(List<float[]> data)
	{
		int swaps = 0 ;
		List<List<float[]>> newClusters = new ArrayList<List<float[]>>();
		for(int j=0;j<k;j++)newClusters.add(new ArrayList<float[]>());
		
		for(int clusterid = 0 ; clusterid< k;clusterid++)
		{
			for(float[] member: clusters.get(clusterid)){
				int nearest = TestUtil.findNearestDistance(member, means);
				newClusters.get(nearest).add(member);
				if(nearest!=clusterid)swaps++;
			}		
		}
		clusters = newClusters;
		return swaps;
	}
	
	
	List<float[]> means; 
	List<List<float[]>> clusters;
	Kmeans(int k, List<float[]> data)
	{
		int maxiters = 10000;
		int swaps = 3;
		this.n = data.size();
		this.d = data.get(0).length;
		this.k = k;
		
		//initialize the clusters to the n/k step first vectors
		means = new ArrayList<float[]>(k);
		for(int i = 0 ; i< k;i++)means.add(data.get(i*(n/k)));
		
		//initialize each cluster gets n/k of the dataset at first
		clusters = new ArrayList<List<float[]>>(k);
		for(int i = 0 ; i< k;i++)
		{
			List<float[]> tmp =  new ArrayList<float[]>(n/k);
			int start = i*(n/k);
			for(int j = 0 ;j<n/k;j++)tmp.add(data.get(j+start));
			clusters.add(tmp);
		}
		
		while(swaps>2 && maxiters>0){
			maxiters--;
			updateMeans();
			swaps = assignClusters(data);
		}
		if(maxiters == 0)System.err.println("Warning: MaxIterations Reached");
	}
	
	List<float[]> getCentroids(){
		return means;
	}


}
