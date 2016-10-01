package edu.uc.rphash;

import java.util.List;

import edu.uc.rphash.Readers.RPHashObject;

public interface Clusterer {
	List<Centroid> getCentroids();
	abstract RPHashObject getParam();
	void setWeights(List<Float> counts);
	void setRawData(List<float[]> centroids);
	void setData(List<Centroid> centroids);
	void setK(int getk);
	/** Reset the clusterers state for a new cluster attempt
	 * @param randomseed
	 */
	void reset(int randomseed);
	/**
	 * Attempt to best of multi-run internally to the clusterer
	 * @param runs
	 * @return true if setting multirun is available, false otherwise
	 */
	boolean setMultiRun(int runs);
}
