package edu.uc.rphash.tests.generators;

import java.util.Iterator;
import java.util.List;

public interface ClusterGenerator{
	abstract public List<float[]> getMedoids();
	abstract public List<float[]> getData();
	abstract public List<Integer> getLabels();
	abstract public int getDimension();
	abstract public Iterator<?> getIterator();
}
