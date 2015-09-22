package edu.uc.rphash;

import java.util.HashSet;

import edu.uc.rphash.Readers.RPVector;

public class Centroid {
	private float[] vec;
	private long count;
	public HashSet<Long> ids;
	public long id;

	public Centroid(int dim, long id) {
		this.vec = new float[dim];
		this.count = 0;
		this.id = id;
		this.ids = new HashSet<Long>();
		ids.add(id);
	}

	public Centroid(float[] data) {
		this.vec = data;
		this.ids = new HashSet<Long>();
		this.count = 1;
	}

	public Centroid(float[] data, long id) {
		this.vec = data;
		this.ids = new HashSet<Long>();
		ids.add(id);
		this.id = id;
		this.count = 1;
	}

	private void updateCentroidVector(float[] data) {
		float delta;
		count++;
		for (int i = 0; i < data.length; i++) {
			float x = data[i];
			delta = x - vec[i];
			vec[i] = vec[i] + delta / count;
		}
	}

	public float[] centroid() {
		return vec;
	}

	public void updateVec(RPVector rp) {
		ids.addAll(rp.id);
		updateCentroidVector(rp.data);
	}

	public void updateVec(float[] rp) {
		updateCentroidVector(rp);
	}

	public long getCount() {
		return count;
	}

	public void addID(long h) {
		if (ids.size() == 0)
			id = h;
		ids.add(h);
	}

}
