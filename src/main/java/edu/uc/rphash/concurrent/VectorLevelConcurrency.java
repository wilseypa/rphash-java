package edu.uc.rphash.concurrent;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.frequentItemSet.KHHCentroidCounter;
import edu.uc.rphash.lsh.LSH;

public class VectorLevelConcurrency implements Runnable {

	private float[] vec;
//	private StatTests vartracker;
	private LSH[] lshfuncs;
	private KHHCentroidCounter is;
	private RPHashObject so;

	public VectorLevelConcurrency(float[] vec, LSH[] lshfuncs,
			/*StatTests vartracker,*/ KHHCentroidCounter is,RPHashObject so) {
		this.vec = vec;
//		this.vartracker = vartracker;
		this.lshfuncs = lshfuncs;
		this.is = is;
		this.so = so;

	}

	static float[] scale(float[] t, float s) {
		float[] ret = new float[t.length];
		for (int i = 0; i < t.length; i++) {
			ret[i] = s*t[i];
		}
		
		return ret;
	}
	
	
	public static long computeSequential(float[] vec,LSH[] lshfuncs,KHHCentroidCounter is,RPHashObject so) {

		Centroid c = new Centroid(vec,-1);
		for (LSH lshfunc : lshfuncs) {
			if (so.getNumBlur() != 1) {
				long[] hash = lshfunc.lshHashRadius(vec, so.getNumBlur());
				
				for (long h : hash) {
					c.addID(h);
					is.addLong(h, 1);
				}
				
			}
			else 
			{
				long hash = lshfunc.lshHash(vec);
				c.addID(hash);
				is.addLong(hash, 1);
			}
			
		}
		is.add(c);
		return is.count;
	}

	@Override
	public void run() {
		computeSequential(this.vec,this.lshfuncs,this.is,this.so);
	}

}
