package edu.uc.rphash.Readers;

import java.util.Iterator;
import java.util.List;

public interface RPHashObject {

	int getk();
	int getn();
	int getdim();
	int getRandomSeed();
	int getHashmod();
	int getTimes();

	Iterator <RPVector>getVectorIterator();
	List<float[]> getCentroids( );
	
	List<Long> getPreviousTopID();
	void setPreviousTopID(List<Long> i);
	

	void addCentroid(float[] v);
	void setCentroids(List<float[]> l);
	
	void setRandomSeed(int seed);
	
	void reset();


}
