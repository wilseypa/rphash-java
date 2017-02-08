package edu.uc.rphash.projections;


public interface Projector {
	float[] project(float[] t);
	void setOrigDim(int n);
	void setProjectedDim(int t);
	void setRandomSeed(long l);
	void init();
	
}
