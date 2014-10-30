package edu.uc.rphash.Readers;

import java.util.List;

public interface RPHashObject {

	int getk();
	int getn();
	int getdim();
	int getRandomSeed();
	int getHashmod();
	int getTimes();
	float[] getNextVector();
	List<Long> getIDs();
	List<Long> getCounts();
	List<float[]>  getCentroids();
	float[] getNextCentroid();
	
	void setIDs(long[] ids);
	void setIDs(List<Long> ids);
	void setCounts(long[] ids);
	void setCounts(List<Long> ids);
	void addCentroid(float[] v);
	void setCentroids(List<float[]> l);
	void reset();


}
