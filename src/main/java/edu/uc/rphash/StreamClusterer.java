package edu.uc.rphash;

import java.util.List;

public interface StreamClusterer extends Clusterer {
	public abstract long addVectorOnlineStep(float[] x);
	public abstract List<Centroid> getCentroidsOfflineStep();
	public abstract void shutdown();
	public int getProcessors();

}
