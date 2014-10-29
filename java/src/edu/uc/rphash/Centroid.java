package edu.uc.rphash;

public class Centroid {
	private float[] vec;
	private long count;
	private float[] centroidvec;
	
	Centroid(int dim){
		this.vec = new float[dim];
		this.count = 0;
		this.centroidvec =null;
	}
	
	public float[] centroid()
	{
		if(centroidvec != null)
			return centroidvec;
		centroidvec = new float[vec.length];
		
		for (int j =0;j<vec.length;j++)
			centroidvec[j] = vec[j]/(float)count;
		
		return centroidvec;
	}

	public void updateVec(float[] n)
	{
		if(count==0){
			for (int j =0;j<vec.length;j++)vec[j] = n[j];
		}
		for (int j =0;j<vec.length;j++)
			vec[j] = vec[j]+n[j];
		count++;
	}

}
