package edu.uc.rphash.concurrent;

import java.util.List;
import java.util.concurrent.RecursiveAction;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.frequentItemSet.KHHCentroidCounter;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.tests.StatTests;

public class VectorLevelConcurrency implements Runnable {

	private float[] vec;
	private StatTests vartracker;
	private RPHashObject so;
	private LSH[] lshfuncs;
	private KHHCentroidCounter is;
	
	public VectorLevelConcurrency(float[] vec,RPHashObject so,LSH[] lshfuncs,StatTests vartracker,KHHCentroidCounter is) {
		this.vec = vec;		
		this.vartracker = vartracker;
		this.so = so;
		this.lshfuncs = lshfuncs;
		this.is = is;
	}

	private void computeSequential(float[] vec){

		long hash[];
//		Centroid c = new Centroid(vec);
//		float tmpvar = vartracker.updateVarianceSample(vec);
		float[] tmpvar = vartracker.updateVarianceSampleVec(vec);
		float[] scaledvec = new float[vec.length]; 
		for(int i = 0;i<vec.length;i++)scaledvec[i] = vec[i]/tmpvar[i];
		for (LSH lshfunc : lshfuncs) 
		{

			Centroid c = new Centroid(vec);
			c.addID(lshfunc.lshHash(scaledvec));
			is.add(c);
//			lshfunc.updateDecoderVariance(tmpvar);
//			hash = lshfunc.lshHashRadiusNo2Hash(scaledvec, so.getNumBlur());
//			for (long h : hash)
//				c.addID(h);
		}
//		is.add(c);
	}
	
	@Override
	public void run() {
			computeSequential(vec);
	}


}
