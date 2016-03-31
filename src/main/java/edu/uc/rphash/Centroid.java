package edu.uc.rphash;

import java.util.concurrent.ConcurrentSkipListSet;

import edu.uc.rphash.Readers.RPVector;

public class Centroid {
	private float[] vec;
	private long count;
	public ConcurrentSkipListSet<Long> ids;
	public long id;
	public int projectionID;

	public Centroid(int dim, long id,int projectionID) {
		this.vec = new float[dim];
		this.count = 0;
		this.id = id;
		this.ids = new ConcurrentSkipListSet<Long>();
		this.projectionID = projectionID;
		ids.add(id);
	}

	public Centroid(float[] data,int projectionID) {
		this.vec = data;
		this.ids = new ConcurrentSkipListSet<Long>();
		this.projectionID = projectionID;
		this.count = 1;
	}

	public Centroid(float[] data, long id,int projectionID) {
		this.vec = data;
		this.ids = new ConcurrentSkipListSet<Long>();
		ids.add(id);
		this.id = id;
		this.projectionID = projectionID;
		this.count = 1;
	}

	private void updateCentroidVector(float[] data) {
		float delta;
		count++;
		for (int i = 0; i < data.length; i++) {
			delta = data[i] - vec[i];
			vec[i] = vec[i] + delta / (float)count;
		}
	}

	public float[] centroid() {
		return vec;
	}

	public void updateVec(Centroid rp) {
		ids.addAll(rp.ids);
		float delta;
		count= count+rp.count;
		for (int i = 0; i < rp.vec.length; i++) {
			delta = rp.vec[i] - rp.vec[i];
			vec[i] = vec[i] + (rp.count*delta) / (float)count;
		}
	}
	

	public void updateVec(float[] rp) {
		updateCentroidVector(rp);
	}

	public long getCount() {
		return count;
	}
	
	public void setCount(long count) {
		this.count = count;
	}

	public void addID(long h) {
		if (ids.size() == 0)
			id = h;
		ids.add(h);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Centroid){
			return ((Centroid)obj).id == id;
		}
		return false;
	}
	

}
