package edu.uc.rphash.frequentItemSet;

import java.util.Iterator;
import java.util.List;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.knee.KneeAlgorithm;

public class KHHCentroidCounterPush extends KHHCentroidCounter {

	int estimatedKnee = 0;
	KneeAlgorithm kne;

	public KHHCentroidCounterPush(float decay, KneeAlgorithm kne) {
		super(1000, decay);
		this.kne = kne;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uc.rphash.frequentItemSet.KHHCentroidCounter#getTop()
	 */
	@Override
	public List<Centroid> getTop() {
		return super.getTop();
	}

	/**
	 * @see edu.uc.rphash.frequentItemSet.KHHCentroidCounter#add(edu.uc.rphash.Centroid)
	 *      This method adds a new vector to the khhcounter and performs knee
	 *      finding on the khhset
	 * @param the
	 *            cluster to be added c
	 * @return the estimated number of clusters using the provided KneeAlgorithm
	 *         if the estimation changes or -1 if it does not
	 */
	public int addAndUpdate(Centroid c) {
		super.add(c);
		// check for new clusters
		int size = frequentItems.values().size();
		float[] counts = new float[size];
		Iterator<Centroid> it = frequentItems.values().iterator();
		for (int i = 0; it.hasNext(); i++) {
			counts[i] = it.next().getCount();
		}
		int tmpknee = kne.findKnee(counts);
		if (tmpknee != estimatedKnee) {
			estimatedKnee = tmpknee;
			return estimatedKnee;
		} else {
			return -1;
		}
	}

}
