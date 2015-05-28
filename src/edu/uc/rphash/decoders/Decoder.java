package edu.uc.rphash.decoders;

public interface Decoder {
	abstract int getDimensionality();
	abstract long[] decode(float[] f);
	abstract float getErrorRadius();
	abstract float getDistance();
}
