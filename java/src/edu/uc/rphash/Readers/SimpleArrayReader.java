package edu.uc.rphash.Readers;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SimpleArrayReader implements RPHashObject {
	
	float[][] X;
	List<List<Float>> Xlist;
	int n;
	int dim;
	int current;
	int randomseed;
	int hashmod;
	int k;
	long[] ids;
	
	public SimpleArrayReader(float[][] X,int k,int randomseed, int hashmod){
		this.X = X;
		this.n = X.length;
		this.dim = X[0].length;
		this.k = k;
		this.randomseed = randomseed;
		this.hashmod = hashmod;
	}
	
	public SimpleArrayReader(List<List<Float>> X){
		this.Xlist = X;
		this.X = null;
		this.n = X.size();
		this.dim = X.get(0).size();
	}
	
	@Override
	public float[] getNextVector() {
		float[] vecX;
		if(X==null)
		{
			vecX = new float[dim];
			List<Float> ptr = Xlist.get(current);
			for(int i =0;i<dim;i++)
				vecX[i] = ptr.get(i);
		}
		else
		{
			vecX = X[current];
		}
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
	public void setIDs(long[] ids) {
		this.ids = ids;
	}

	@Override
	public long[] getIDs() {
		return ids;
	}
	
	public void setIDs(Set<Long> ids){
		this.ids = new long[ids.size()];
		Iterator<Long> it = ids.iterator();
		int i = 0;
		while(it.hasNext())
			this.ids[i++] = it.next();
	}

	@Override
	public void reset() {
		current = 0;
	}

}
