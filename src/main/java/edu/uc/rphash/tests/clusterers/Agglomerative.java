package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.List;

import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

public class Agglomerative implements Clusterer{
	
	int k;
	List<float[]> clusters;
	List<float[]> data;
	float[][] distances;
	List<Integer> counts;
	
	public Agglomerative(int k, List<float[]> data)
	{
		this.k = k;
		this.data = data;
		this.clusters = null;
		counts = new ArrayList<Integer>();
		for(int i = 0;i<data.size();i++)counts.add(1);
		distanceArray(data);
	} 
	
	public static double[][] distanceArray(List<float[]> data){
		double[][] distances = new double[data.size()][data.size()];
		for(int i = 0 ; i < data.size();i++)
		{
			for(int j = 0; j < data.size();j++)
				distances[i][j] =VectorUtil.distance(data.get(i), data.get(j));
		}
		return distances;
	}
	
	
	private float[] avgVector(float[] u, float[] v, int ct1, int ct2){
		float[] w = new float[u.length];
		for(int i = 0 ;i < u.length;i++)w[i] = (u[i]*ct1+v[i]*ct2)/(ct1+ct2);
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
		data.set(mini, avgVector(data.get(mini), data.get(minj),counts.get(mini),counts.get(minj)));
		counts.set(minj,counts.get(mini)+counts.get(minj));
		counts.remove(minj);
		data.remove(minj);
		distanceArray(data);
		
	}
	
	void run()
	{
		while(data.size()>k)
			merge();
	}
	
	public static void main(String[] args){
		GenerateData gen =  new GenerateData(3,100,2);
		List<float[]> data =gen.data;
		
		double[][] dists = distanceArray(data);
		
	
		double[] weights = new double[data.size()];
		
		
		
		String[] s = new String[dists.length];
		for(int i = 0;i< dists.length;i++)s[i] = String.valueOf(i);


		
		
		Agglomerative agl = new Agglomerative(3,  data);
		agl.run();
		for(float[] cent: gen.getMedoids()){
			for(float f : cent)System.out.print(f+" ");
			System.out.println();
		}
		System.out.println("computed");
		
		for(float[] cent: agl.getCentroids()){
			for(float f : cent)System.out.print(f+" ");
			System.out.println();
		}

	}

	@Override
	public List<float[]> getCentroids() {
		run();
		return data;
	}

	@Override
	public RPHashObject getParam() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	

}
