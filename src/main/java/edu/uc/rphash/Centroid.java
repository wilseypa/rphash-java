package edu.uc.rphash;

import java.util.concurrent.ConcurrentSkipListSet;

import edu.uc.rphash.Readers.RPVector;

public class Centroid {
	public float[] centroid;
	public long count;
	public int dimensions;
	public ConcurrentSkipListSet<Long> ids;
	public long id;
	public int projectionID;
//	private float[] M2;
	public float[] wcss;

	public Centroid(int dim, long id, int projectionID) {
		this.centroid = new float[dim];
		this.dimensions = dim;
		this.wcss = new float[dim];
		this.count = 0;
		this.id = id;
		this.ids = new ConcurrentSkipListSet<Long>();
		this.projectionID = projectionID;
		ids.add(id);
	}

	public Centroid(float[] data, int projectionID) {
		this.centroid = data;
		this.dimensions = data.length;
		this.wcss = new float[this.centroid.length];
		this.ids = new ConcurrentSkipListSet<Long>();
		this.projectionID = projectionID;
		this.count = 1;
	}

	public Centroid(float[] data, long id, int projectionID) {
		this.centroid = data;
		this.dimensions = data.length;
		this.wcss = new float[this.centroid.length];
		this.ids = new ConcurrentSkipListSet<Long>();
		ids.add(id);
		this.id = id;
		this.projectionID = projectionID;
		this.count = 1;
	}
	
	public Centroid(long count,float[] data) {
		this.centroid = data;
		this.dimensions = data.length;
		this.wcss = new float[this.centroid.length];
		this.ids = new ConcurrentSkipListSet<Long>();
		this.projectionID = -1;
		this.count = 1;
	}
	
	public Centroid(float[] data) {
		this.centroid = data;
		this.dimensions = data.length;
		this.wcss = new float[this.centroid.length];
		this.ids = new ConcurrentSkipListSet<Long>();
		this.projectionID = -1;
		this.count = 1;
	}
	
	public Centroid(long count,float[] data, float[] wcss) {
		this.centroid = data;
		this.dimensions = data.length;
		if(wcss!=null)this.wcss = wcss;
		else this.wcss = new float[data.length];
		this.ids = new ConcurrentSkipListSet<Long>();
		this.count = count;
		this.projectionID = -1;
		this.wcss = wcss;
	}

	public float[] centroid() {
		return centroid;
	}

	public float[] getWCSS() {
		return this.wcss;
	}
	
	
	
	/** This is the merge function tested in python and found to be the most stable for randomly distributed and
	 * skewed data. Previous attempts to merge wcss had issues,
	 * @param cnt_1
	 * @param x_1
	 * @param var_1
	 * @param cnt_2
	 * @param x_2
	 * @return [count,mean,var]
	 */
	public static float[][] merge(float cnt_1, float[] x_1, float[] var_1,
			float cnt_2, float[] x_2) {
		float cnt_r = cnt_1 + cnt_2;
		float[] x_r = new float[x_1.length];
		float[] var_r = new float[x_1.length];
		for (int i = 0; i < x_1.length; i++) {
			x_r[i] = (cnt_1 * x_1[i] + cnt_2 * x_2[i]) / cnt_r;
			var_r[i] += cnt_1
					* ((x_r[i] - x_1[i]) * (x_r[i] - x_1[i]) + var_1[i])
					+ cnt_2
					* ((x_r[i] - x_2[i]) * (x_r[i] - x_2[i]));
			var_r[i] = var_r[i] / cnt_r;
		}

		float[][] ret = new float[3][];
		ret[0] = new float[1];
		ret[0][0] = cnt_r;
		ret[1] = x_r;
		ret[2] = var_r;
		return ret;
	}
	
	/** This is the merge function tested in python and found to be the most stable for randomly distributed and
	 * skewed data. Previous attempts to merge wcss had issues,
	 * @param cnt_1
	 * @param x_1
	 * @param var_1
	 * @param cnt_2
	 * @param x_2
	 * @param var_2
	 * @return [count,mean,var]
	 */
	public static float[][] merge(float cnt_1, float[] x_1, float[] var_1,
			float cnt_2, float[] x_2, float[] var_2) {
		float cnt_r = cnt_1 + cnt_2;
		float[] x_r = new float[x_1.length];
		float[] var_r = new float[x_1.length];
		for (int i = 0; i < x_1.length; i++) {
			x_r[i] = (cnt_1 * x_1[i] + cnt_2 * x_2[i]) / cnt_r;
			var_r[i] += cnt_1
					* ((x_r[i] - x_1[i]) * (x_r[i] - x_1[i]) + var_1[i])
					+ cnt_2
					* ((x_r[i] - x_2[i]) * (x_r[i] - x_2[i]) + var_2[i]);
			var_r[i] = var_r[i] / cnt_r;
		}

		float[][] ret = new float[3][];
		ret[0] = new float[1];
		ret[0][0] = cnt_r;
		ret[1] = x_r;
		ret[2] = var_r;
		return ret;
	}
	
	public void mergeCentroids(Centroid rp) {
		ids.addAll(rp.ids);
		float[][] ret = merge(this.count, this.centroid, this.wcss, rp.count, rp.centroid,
				rp.wcss);
		this.count = (long) ret[0][0];
		this.centroid =  ret[1];
		this.wcss = ret[2];
//		this.M2 = ret[2];
	}

	public void updateVec(float[] data) 
	{
		float[][] ret = merge(this.count, this.centroid, this.wcss, 1, data);
		this.count = (long) ret[0][0];
		this.centroid =  ret[1];
		this.wcss = ret[2];
//		this.M2 = ret[2];
		
//	float delta;
//	count++;
////	float tmpsum = 0.0f;
//	for (int i = 0; i < data.length; i++) {
//		delta = data[i] - centroid[i];
//		centroid[i] = centroid[i] + delta / (float) count;
//		this.wcss[i] = this.wcss[i] + delta * data[i] - centroid[i];
////		tmpsum += M2[i];
//	}
//	this.wcss = M2;//tmpsum / (float) count;
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
