package edu.uc.rphash.centroidTracker;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.frequentItemSet.KHHCentroidCounter;
import edu.uc.rphash.lsh.LSH;

public class trackCentroids implements Runnable {

	private float[] vec;


	public trackCentroids(float[] vec, LSH[] lshfuncs) {

	}

	static float[] scale(float[] t, float s) {
		float[] ret = new float[t.length];
		for (int i = 0; i < t.length; i++) {
			ret[i] = s*t[i];
		}
		
		return ret;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	

}

		


