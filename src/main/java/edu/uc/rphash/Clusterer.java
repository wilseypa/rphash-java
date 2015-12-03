package edu.uc.rphash;

import java.util.List;

import edu.uc.rphash.Readers.RPHashObject;

public interface Clusterer {
	List<float[]> getCentroids();
	abstract RPHashObject getParam();

}
