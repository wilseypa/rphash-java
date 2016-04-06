package edu.uc.rphash.concurrent;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.frequentItemSet.KHHCentroidCounter;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.tests.StatTests;

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

	public static long computeSequential(float[] vec,LSH[] lshfuncs,KHHCentroidCounter is,RPHashObject so) {
//		if(!lshfuncs[0].lshDecoder.selfScaling()){
//			this.vartracker.updateVarianceSampleVec(vec);
//			vec = this.vartracker.scaleVector(vec);
//		}
		Centroid c = new Centroid(vec,-1);
		for (LSH lshfunc : lshfuncs) {
			if (so.getNumBlur() != 1) {
				long[] hash = lshfunc
						.lshHashRadiusNo2Hash(vec, so.getNumBlur());
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
