package edu.uc.rphash.Readers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SimpleArrayReader implements RPHashObject {
	
	List<RPVector> data;
	//List<List<Float>> Xlist;
	int n;
	int dim;

	int randomseed;
	int hashmod;
	int k;
	int times;

	List<float[]> centroids;
	List<Long> topIDs;
	
	
	
	public SimpleArrayReader(List<float[]> X,int k,int randomseed, int hashmod,int times){
		
		data = new LinkedList<RPVector>();
		for(int i = 0 ; i < X.size();i++){
			RPVector r = new RPVector();
			r.data = X.get(i);
			r.count = 0;
			r.id = new HashSet<Long>() ;
			data.add(r);
		}
		
		this.n = X.size();
		this.dim = X.get(0).length;
		this.k = k;
		this.randomseed = randomseed;
		this.hashmod = hashmod;

		centroids =  new ArrayList<float[]>();;
		this.times = times;
		this.topIDs = new ArrayList<Long>();
		for(int i = 0 ; i < k;i++)topIDs.add((long) 0);
		
		
	}
	
	
	public Iterator<RPVector> getVectorIterator() {
		return data.iterator();
	}


	
	@Override
	public int getk() {
		return k;
	}

	@Override
	public int getn() {
		return data.size();
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
		
	}
	
	@Override
	public void addCentroid(float[] v) {
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
	public int getTimes() {
		return times;
	}

	@Override
	public List<Long> getPreviousTopID() {
		return topIDs;
	}

	@Override
	public void setPreviousTopID(List<Long> top) {
		topIDs=top;		
	}
	@Override
	public void setRandomSeed(int seed){
		this.randomseed = seed;
	}

}
