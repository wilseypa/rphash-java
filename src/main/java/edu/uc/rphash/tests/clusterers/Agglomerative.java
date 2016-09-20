package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.List;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

public class Agglomerative implements Clusterer{
	
	int k;
	List<float[]> clusters;
	List<float[]> data;
	float[][] distances;
	List<Float> counts;
	public Agglomerative()
	{
		
	}
	public Agglomerative(int k, List<float[]> data)
	{
		this.k = k;
		this.data = data;
		this.clusters = null;
		counts = new ArrayList<Float>();
		for(int i = 0;i<data.size();i++)counts.add(1f);
	} 
	
	public static float[][] distanceArray(List<float[]> data){
		float[][] distances = new float[data.size()][data.size()];
	
		for(int i = 0 ; i < data.size();i++)
		{
			for(int j = 0; j < data.size();j++)
				distances[i][j] =VectorUtil.distance(data.get(i), data.get(j));
		}
		return distances;
	}
	
	
	private float[] avgVector(float[] u, float[] v, Float float1, Float float2){
		float[] w = new float[u.length];
		for(int i = 0 ;i < u.length;i++)w[i] = (u[i]*float1+v[i]*float2)/(float1+float2);
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
		this.distances = distanceArray(data);
		while(data.size()>k)
			merge();
	}
	
	public static void main(String[] args){
		GenerateData gen =  new GenerateData(3,500,2);
		List<float[]> data =gen.data;
		float[][] dists = distanceArray(data);
//		double[] weights = new double[data.size()];
		
		
		String[] s = new String[dists.length];
		for(int i = 0;i< dists.length;i++)s[i] = String.valueOf(i);
		
		Agglomerative agl = new Agglomerative(3,  data);
		agl.run();
		for(float[] cent: gen.getMedoids()){
			for(float f : cent)System.out.print(f+" ");
			System.out.println();
		}
		System.out.println("computed");
		
		for(Centroid cent: agl.getCentroids()){
			for(float f : cent.centroid())System.out.print(f+" ");
			System.out.println();
		}

	}

	@Override
	public List<Centroid> getCentroids() {
		if(clusters==null)run();
		List<Centroid> cents = new ArrayList<>(clusters.size());
		for(float[] v : this.clusters)cents.add(new Centroid(v,0));
		return cents;
	}
	
	@Override
	public void reset(int randomseed) {
		clusters = null;
	}


	@Override
	public RPHashObject getParam() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWeights(List<Float> counts) {
		//this.counts = counts;
		counts = new ArrayList<Float>();
	}
	@Override
	public void setK(int getk) {
		this.k = getk;
	}
	@Override
	public void setData(List<Centroid> centroids) {
		this.data = new ArrayList<float[]>(centroids.size());
		for(Centroid c : centroids) data.add(c.centroid());
	}
	@Override
	public void setRawData(List<float[]> centroids) {
		this.data = centroids;
	}
}
