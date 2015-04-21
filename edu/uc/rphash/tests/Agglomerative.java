package edu.uc.rphash.tests;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

import edu.uc.rphash.Clusterer;

public class Agglomerative implements Clusterer{
	
	int k;
	List<float[]> clusters;
	List<float[]> data;
	float[][] distances;
	
	public Agglomerative(int k, List<float[]> data)
	{
		this.k = k;
		this.data = data;
		this.clusters = null;
		
		distanceArray(data);
	} 
	
	private void distanceArray(List<float[]> data){
		distances = new float[data.size()][data.size()];
		for(int i = 0 ; i < data.size();i++)
		{
			for(int j = 0; j < data.size();j++)
				distances[i][j] =TestUtil.distance(data.get(i), data.get(j));
		}
		
	}
	
	
	private float[] avgVector(float[] u, float[] v){
		float[] w = new float[u.length];
		for(int i = 0 ;i < u.length;i++)w[i] = (u[i]+v[i])/2f;
		return w;
	}
	
	private void merge()
	{
		float minimum = 1000000f;
		int mini = 0;
		int minj = 0;
		int i = 0 ;
		for(float[] l : distances)
		{
			for(int j = 0; j < data.size();j++){
				if(l[j]<minimum){
					minimum = l[j];
					mini = i;
					minj = j;
				}
			}
			i++;
		}
		data.set(mini, avgVector(data.get(mini), data.get(minj)));
		data.remove(minj);
		distanceArray(data);
		
	}
	
	private void run()
	{
		while(data.size()>k)
			merge();
	}
	
	public static void main(String[] args){
		List<float[]> data = new GenerateData(5,10,2).data;
		Agglomerative agl = new Agglomerative(3,  data);
		agl.run();
	}

	@Override
	public List<float[]> getCentroids() {
		run();
		return data;
	}
	
	
	

}
