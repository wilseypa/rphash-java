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
	private float variance;
	private KHHCentroidCounter is;
	
	public VectorLevelConcurrency(float[] vec,RPHashObject so,LSH[] lshfuncs,StatTests vartracker,float variance,KHHCentroidCounter is) {
		this.vec = vec;		
		this.vartracker = vartracker;
		this.so = so;
		this.lshfuncs = lshfuncs;
		this.is = is;
		this.variance = variance;
	}

	private void computeSequential(float[] vec){

		long hash[];
		Centroid c = new Centroid(vec);
		float tmpvar = vartracker.updateVarianceSample(vec);
		if(variance!=tmpvar){
			for (LSH lshfunc : lshfuncs) 
				lshfunc.updateDecoderVariance(tmpvar);
			variance= tmpvar;
		}
		for (LSH lshfunc : lshfuncs) 
		{
			hash = lshfunc.lshHashRadiusNo2Hash(vec, so.getNumBlur());
			for (long h : hash)
				c.addID(h);
		}
		is.add(c);
	}
	
	@Override
	public void run() {

			computeSequential(vec);
		
	}


}
