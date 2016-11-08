package edu.uc.rphash.decoders;

public interface Decoder {

	abstract int getDimensionality();
	abstract long[] decode(float[] f);
	abstract float getErrorRadius();
	abstract float getDistance();
	
	abstract boolean selfScaling();
//	abstract float[] getVariance();
//	abstract void setVariance(float[] parameterObject);
}
