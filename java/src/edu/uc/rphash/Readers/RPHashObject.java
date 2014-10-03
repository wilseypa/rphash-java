package edu.uc.rphash.Readers;

import java.util.Set;

public interface RPHashObject {

	int getk();
	int getn();
	int getdim();
	int getRandomSeed();
	int getHashmod();
	float[] getNextVector();
	void setIDs(long[] ids);
	void setIDs(Set<Long> ids);
	void reset();
	long[] getIDs();

}
