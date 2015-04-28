package edu.uc.rphash.Readers;

import java.util.Iterator;
import java.util.List;

import edu.uc.rphash.decoders.Decoder;

public interface RPHashObject {

	int getk();
	int getn();
	int getdim();
	long getRandomSeed();
	long getHashmod();
	int getNumBlur();
	Iterator<float[]> getVectorIterator();
	List<float[]> getCentroids( );
	
	List<Long> getPreviousTopID();
	void setPreviousTopID(List<Long> i);
	

	void addCentroid(float[] v);
	void setCentroids(List<float[]> l);
	
	void reset();
	int getNumProjections();
	void setNumProjections(int probes);
	void setInnerDecoderMultiplier(int multiDim);
	int getInnerDecoderMultiplier();
	void setNumBlur(int parseInt);
	void setRandomSeed(long parseLong);
	void setHashMod(long parseLong);
	void setDecoderType(Decoder dec);
	Decoder getDecoderType();
	String toString();


}
