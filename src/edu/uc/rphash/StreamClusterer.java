package edu.uc.rphash;

import java.util.List;

public interface StreamClusterer extends Clusterer {
	public abstract int addVector(float[] x);
	public abstract List<float[]> getCentroidsOnline();

}
