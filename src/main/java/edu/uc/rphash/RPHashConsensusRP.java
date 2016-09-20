package edu.uc.rphash;

import java.util.ArrayList;
import java.util.List;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.Agglomerative;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

public class RPHashConsensusRP  implements Clusterer{

	private List<Centroid> centroids=null;
	int k;
	int d;
	RPHashObject so;
	
	public RPHashConsensusRP(List<float[]> data,int k){

		this.k =k;
		this.d = data.get(0).length;
		this.so = new SimpleArrayReader(data,k);
	}
	
	public RPHashConsensusRP(RPHashObject o) {

		this.k =o.getk();

		this.d = o.getdim();
		this.so = o;
	}
	


	@Override
	public List<Centroid> getCentroids(){
		
		if(centroids == null)run();
		return centroids;
	}
	
	private  void run()
	{
			
		
//		for(int j =0;j<k;j++)
//		{
//			float[] initCentroids = new float[d];
//			float[] centptr = rp.getCentroids().get(j);
//			//deep copy
//			for(int b = 0;b<k;b++)initCentroids[b] = centptr[b];
//			counts[j] = 1.0f;
//			prevCentroids.add(initCentroids);
//		}
		
		ArrayList<Centroid> manyCentroids = new ArrayList<Centroid> (k);
		manyCentroids.addAll(new RPHashSimple (so).getCentroids());
		manyCentroids.addAll(new RPHash3Stage (so).getCentroids());
		manyCentroids.addAll(new RPHashIterativeRedux(so).getCentroids());
		//stream, multiproj, are simply sub versions of rphash.
		//sphereical is not complete
		Clusterer offlineclusterer = so.getOfflineClusterer();
		offlineclusterer.setWeights(so.getCounts());
		offlineclusterer.setData(so.getCentroids());
		offlineclusterer.setK(so.getk());
		centroids = offlineclusterer.getCentroids();
		
	}
	
	public static GenerateData gen;
	public static void main(String[] args){
		
		int k = 20;
		int d = 5000;	
		int n = 10000;
		gen = new GenerateData(k,n/k,d,.1f,true,.1f);
		RPHashConsensusRP rphit = new RPHashConsensusRP(gen.data(),k);
		
		long startTime = System.nanoTime();
		rphit.getCentroids();
		long duration = (System.nanoTime() - startTime);
//		List<float[]> aligned  = VectorUtil.alignCentroids(rphit.getCentroids(),gen.medoids());
//		System.out.println(StatTests.PR(aligned,gen)+":"+duration/1000000000f);
//		System.out.print("\n");
		System.gc();
		
	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

	@Override
	public void setWeights(List<Float> counts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setK(int getk) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setData(List<Centroid> centroids) {
		this.centroids = centroids;
		
	}
	@Override
	public void setRawData(List<float[]> centroids) {
		if(this.centroids == null) this.centroids = new ArrayList<>(centroids.size());
		for(float[] f: centroids){
			this.centroids.add(new Centroid(f,0));
		}
	}
	@Override
	public void reset(int randomseed) {
		centroids = null;
		so.setRandomSeed(randomseed);
	}
	
	
	
}
