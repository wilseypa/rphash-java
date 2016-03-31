package edu.uc.rphash.concurrent;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.frequentItemSet.KHHCentroidCounter;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.tests.StatTests;

public class VectorLevelConcurrency implements Runnable {

	private float[] vec;
	private StatTests vartracker;
	private LSH[] lshfuncs;
	private KHHCentroidCounter is;
	private RPHashObject so;

	public VectorLevelConcurrency(float[] vec, LSH[] lshfuncs,
			StatTests vartracker, KHHCentroidCounter is,RPHashObject so) {
		this.vec = vec;
		this.vartracker = vartracker;
		this.lshfuncs = lshfuncs;
		this.is = is;
		this.so = so;
	}

	private void computeSequential(float[] vec) {


//		if(!lshfuncs[0].lshDecoder.selfScaling()){
//			this.vartracker.updateVarianceSampleVec(vec);
//			vec = this.vartracker.scaleVector(vec);
//		}
		int i =0;
		
		for (LSH lshfunc : lshfuncs) {
			if (so.getNumBlur() != 1) {
				long[] hash = lshfunc
						.lshHashRadiusNo2Hash(vec, so.getNumBlur());
				for (long h : hash) {
					Centroid c = new Centroid(vec,-1);
					c.addID(h);
					is.addLong(h, 1);
					is.add(c);
				}
			} else {
				Centroid c = new Centroid(vec,-1);
				long hash = lshfunc.lshHash(vec);
				c.addID(hash);
				is.addLong(hash, 1);
				is.add(c);
			}
		}
//		is.add(c);
	}

	@Override
	public void run() {
		computeSequential(vec);
	}

}
