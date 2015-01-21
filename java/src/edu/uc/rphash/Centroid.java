package edu.uc.rphash;

import java.util.HashSet;

import edu.uc.rphash.Readers.RPVector;
import edu.uc.rphash.tests.TestUtil;

public class Centroid {
	private float[] vec;
	private long count;
	private float[] centroidvec;
	public HashSet<Long> ids;
	//public float[] variance;
	//float[] M2;

	
	Centroid(int dim,long id)
	{
		this.vec = new float[dim];

		this.count = 0;
		this.ids = new HashSet<Long>();
		ids.add(id);
		//this.variance = new float[dim];
		//this.M2 =  new float[dim];
		
	}
	private void updateVariance(float[] data){
				float delta;
	    	    for(int i =0;i<data.length;i++){
	    	    		float x = data[i];
	    	            delta = x - vec[i];
	    	            vec[i] = vec[i] + delta/(float)count;
	    	            //M2[i] = M2[i] + delta*(x - vec[i]);
	    	            //variance[i] = M2[i]/((float)count-1);
	    	    }
	    	    
	}
	
	public float[] centroid()
	{
		if(vec != null)
			return vec;

		centroidvec = new float[vec.length];
		
		for (int j =0;j<vec.length;j++)
			centroidvec[j] = vec[j]/(float)count;
		
		return centroidvec;
	}
//comment out to record variance
	public void updateVec(RPVector rp)
	{
		ids.addAll(rp.id);
		if(count==0){
			for (int j =0;j<vec.length;j++){
				vec[j] = rp.data[j];
				//variance[j] = rp.data[j];
			}
		}
//		for (int j =0;j<vec.length;j++)
//			vec[j] = vec[j]+rp.data[j];
		count++;
		updateVariance(rp.data);
	}
	
	public void updateVec(float[] rp)
	{
		//ids.addAll(rp.id);
		if(count==0){
			for (int j =0;j<vec.length;j++){
				vec[j] = rp[j];
				//variance[j] = rp[j];
			}
			
		}
//		for (int j =0;j<vec.length;j++)
//			vec[j] = vec[j]+rp[j];
		count++;
		updateVariance(rp);
	}
	public long getCount() {
		return count;
	}

}
