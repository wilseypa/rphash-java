package edu.uc.rphash.projections;

import java.util.Random;

public class NoProjection implements Projector {

	int n;
	int t;



	public NoProjection(int n, int t, int randomseed) {
		this.n = n;
		this.t = t;
	}
	
	public NoProjection(int n, int t) {
		this.n = n;
		this.t = t;
	}


	public NoProjection() {
	}

	@Override
	public float[] project(float[] s) {
		float[] ret = new float[t];
		for(int i = 0;i<t;i++)ret[i] = s[i];
		return ret;
	}

	@Override
	public void setOrigDim(int n) {
		this.n = n;
	}

	@Override
	public void setProjectedDim(int t) {
		this.t = t;
	}

	@Override
	public void setRandomSeed(long l) {
		
	}

	@Override
	public void init() {
	}

}
