package uc.edu.rphash.decoders;

public interface Decoder {
	abstract int getDimensionality();
	abstract byte[] decode(float[] f);
}
