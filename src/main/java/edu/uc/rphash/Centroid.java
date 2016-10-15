package edu.uc.rphash;

import java.util.concurrent.ConcurrentSkipListSet;

import edu.uc.rphash.Readers.RPVector;

public class Centroid {
	private float[] centroid;
	private long count;
	public ConcurrentSkipListSet<Long> ids;
	public long id;
	public int projectionID;
	private float[] M2;
	private float[] wcss;

	public Centroid(int dim, long id, int projectionID) {
		this.centroid = new float[dim];
		this.M2 = new float[dim];
		this.count = 0;
		this.id = id;
		this.ids = new ConcurrentSkipListSet<Long>();
		this.projectionID = projectionID;
		ids.add(id);
	}

	public Centroid(float[] data, int projectionID) {
		this.centroid = data;
		this.M2 = new float[this.centroid.length];
		this.ids = new ConcurrentSkipListSet<Long>();
		this.projectionID = projectionID;
		this.count = 1;
	}

	public Centroid(float[] data, long id, int projectionID) {
		this.centroid = data;
		this.M2 = new float[this.centroid.length];
		this.ids = new ConcurrentSkipListSet<Long>();
		ids.add(id);
		this.id = id;
		this.projectionID = projectionID;
		this.count = 1;

	}

	private void updateCentroidVector(float[] data) {
			float delta;
			count++;
			float tmpsum = 0.0f;
			for (int i = 0; i < data.length; i++) {
				delta = data[i] - centroid[i];
				centroid[i] = centroid[i] + delta / (float) count;
				M2[i] = M2[i] + delta * data[i] - centroid[i];
				tmpsum += M2[i];
			}
			this.wcss = M2;//tmpsum / (float) count;
	}

	public float[] centroid() {
		return centroid;
	}

	public void updateVec(Centroid rp) {
		ids.addAll(rp.ids);
		float delta;
		count = count + rp.count;
		float tmpsum = 0.0f;
		for (int i = 0; i < rp.centroid.length; i++) {
			delta = rp.centroid[i] - centroid[i];
			centroid[i] = centroid[i] + (rp.count * delta) / (float) count;
			M2[i] = M2[i] + rp.count * delta * rp.centroid[i] - centroid[i];
			tmpsum += M2[i];
		}
		this.wcss = M2;//tmpsum / (float) count;
	}

	public float[] getWCSS() {
		return this.wcss;
	}

	public void updateVec(float[] rp) {
		updateCentroidVector(rp);
	}

	public Long getCount() {
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
		if (obj instanceof Centroid) {
			return ((Centroid) obj).id == id;
		}
		return false;
	}

	public void setWCSS(double[] d) {	
		if(this.wcss==null)this.wcss = new float[d.length];
		for(int i = 0;i<d.length;i++)
			this.wcss[i] = (float) d[i];
	}
	
	public void setWCSS(float[] d) {	
		this.wcss =  d;
	}

}
