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

}
