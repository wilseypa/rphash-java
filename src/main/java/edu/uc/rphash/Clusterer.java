package edu.uc.rphash;

import java.util.List;

import edu.uc.rphash.Readers.RPHashObject;

public interface Clusterer {
	List<float[]> getCentroids();
	abstract RPHashObject getParam();
	void setWeights(List<Float> counts);
	void setData(List<float[]> centroids);
	void setK(int getk);

}
