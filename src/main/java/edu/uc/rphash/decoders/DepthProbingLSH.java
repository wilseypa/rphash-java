package edu.uc.rphash.decoders;

import edu.uc.rphash.frequentItemSet.Countable;
import edu.uc.rphash.util.VectorUtil;

public class DepthProbingLSH implements Decoder {
	
	int dim;
	public DepthProbingLSH(int dim){
		this.dim = dim;
	}
	
	
	public DepthProbingLSH(Countable counter, int dim){
		this.counter = counter;
		this.dim = dim;
	}

	@Override
	public int getDimensionality() {
		return dim;
	}
	

	
	/*
	 * foreseeable issues, cold start is broken
	 * 1<1.... since all counts start at 0
	 * 
	 * we may need alot of tuning
	 * 
	 * things that start with zeros will be misread as missing entropy
	 * 		how bad is this? 
	 * 		at least half are full bit width, and the probability of 
	 * 		two bits off is have of that, in essence it is exponentially
	 * 		fleating that our counts are off by alot for bit entropy
	 * (non-Javadoc)
	 * @see edu.uc.rphash.decoders.Decoder#decode(float[])
	 */
	
	@Override
	public long[] decode(float[] f) {
		
		long  recursiveHash =0;
		float parentCount = 0;
		
		if(f[0]>0)recursiveHash+=1;
		counter.add(recursiveHash);
		parentCount = counter.count(recursiveHash);
		
		for(int i = 1;i<f.length;i++){
			recursiveHash<<=1;
			if(f[i]>0)recursiveHash+=1;
			counter.add(recursiveHash);
			float curcount = counter.count(recursiveHash);
			if((curcount+curcount)<parentCount)
			{
				return new long[]{recursiveHash};
			}
			parentCount = curcount;
		}
		return new long[]{recursiveHash};
	}
	
	Countable counter;
	@Override
	public void setCounter(Countable counter) {
		this.counter = counter;
	}

	@Override
	public float getErrorRadius() {
		return -1;
	}

	@Override
	public float getDistance() {
		return 0;
	}

	@Override
	public boolean selfScaling() {
		return true;
	}

}
