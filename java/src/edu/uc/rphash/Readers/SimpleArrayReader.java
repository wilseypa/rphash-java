package edu.uc.rphash.Readers;

import java.util.ArrayList;
import java.util.List;

public class SimpleArrayReader implements RPHashObject {
	
	List<float[]> X;
	//List<List<Float>> Xlist;
	int n;
	int dim;
	int current;
	int curCentroid;
	int randomseed;
	int hashmod;
	int k;
	List<Long> ids;
	List<Long> counts;
	List<float[]> centroids;
	
	public SimpleArrayReader(List<float[]> X,int k,int randomseed, int hashmod){
		this.X = X;
		this.n = X.size();
		this.dim = X.get(0).length;
		this.k = k;
		this.randomseed = randomseed;
		this.hashmod = hashmod;
		curCentroid = 0;
		current = 0;
		centroids = null;
	}
	
//	public SimpleArrayReader(List<List<Float>> X,int k,int randomseed, int hashmod){
//		this.Xlist = X;
//		this.X = null;
//		this.n = X.size();
//		this.dim = X.get(0).size();
//		this.k = k;
//		this.randomseed = randomseed;
//		this.hashmod = hashmod;
//		curCentroid = 0;
//		current = 0;
//		centroids = null;
//	}
	
	@Override
	public float[] getNextVector() {
		if(current >= this.n)return null;
		float[] vecX;
//		if(X==null)
//		{
//			vecX = new float[dim];
//			List<Float> ptr = Xlist.get(current);
//			for(int i =0;i<dim;i++)
//				vecX[i] = ptr.get(i);
//		}
//		else
//		{
			vecX = X.get(current);
//		}
		current++;
		return vecX;
	}

	@Override
	public int getk() {
		return k;
	}

	@Override
	public int getn() {
		return n;
	}

	@Override
	public int getdim() {
		return dim;
	}
	public int getHashmod(){
		return hashmod;
	}
	@Override
	public int getRandomSeed(){
		return randomseed;
	}
	
	@Override
	public void reset() {
		current = 0;
	}
	
	@Override
	public void setIDs(long[] ids) {
		this.ids = new ArrayList<Long>(ids.length);
		for(int i=0;i<this.ids.size();i++)this.ids.add(ids[i]);
	}
	
	public void setIDs(List<Long> ids){
		this.ids = ids;
	}

	@Override
	public void setCounts(long[] counts) {
		this.counts = new ArrayList<Long>(counts.length);
		for(int i=0;i<counts.length;i++)this.counts.add(counts[i]);
	}

	@Override
	public void setCounts(List<Long> counts) {
		this.counts = counts;
	}

	@Override
	public List<Long> getIDs() {
		return ids;
	}

	@Override
	public void addCentroid(float[] v) {
		if(centroids==null)centroids = new ArrayList<float[]>();
		centroids.add(v);
	}

	@Override
	public void setCentroids(List<float[]> l) {
		centroids = l;
		
	}

	@Override
	public List<float[]> getCentroids() {
		return centroids;
	}

	@Override
	public float[] getNextCentroid() {
		if(curCentroid >=k)return null;
		return centroids.get(curCentroid++);
	}

}
