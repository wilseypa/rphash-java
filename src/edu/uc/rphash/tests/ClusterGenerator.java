package edu.uc.rphash.tests;

import java.util.List;

public interface ClusterGenerator {
	abstract public List<float[]> getMedoids();
	abstract public List<float[]> getData();
	abstract public List<Integer> getLabels();

}
