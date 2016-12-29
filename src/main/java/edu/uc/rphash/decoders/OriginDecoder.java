package edu.uc.rphash.decoders;

import edu.uc.rphash.frequentItemSet.Countable;
import edu.uc.rphash.util.VectorUtil;

public class OriginDecoder implements Decoder {

	float[] u, v;
	int n;
	float dist;

	public OriginDecoder(int n) {
		this.n = n;
	}
	
	@Override
	public int getDimensionality() {
		return this.n;
	}

	@Override
	public long[] decode(float[] f) {
		long[]  ret = new long[1];
		
		if(f[0]>0)ret[0]+=1;
		for(int i = 1;i<f.length;i++){
			ret[0]<<=1;
			if(f[i]>0)ret[0]+=1;
		}
		return ret;
	}

	@Override
	public float getErrorRadius() {
		return 0;
	}

	@Override
	public float getDistance() {
		return 0;
	}

	@Override
	public boolean selfScaling() {
		return true;
	}

	@Override
	public void setCounter(Countable counter) {
		// TODO Auto-generated method stub
		
	}


}
