package edu.uc.rphash;

import java.util.HashSet;

import edu.uc.rphash.Readers.RPVector;
import edu.uc.rphash.tests.TestUtil;

public class Centroid {
	private float[] vec;
	private long count;
	public HashSet<Long> ids;
//	public float[] variance;
//	float[] M2;
//	float[] sumvec;

	
	Centroid(int dim,long id)
	{
		this.vec = new float[dim];

		this.count = 0;
		this.ids = new HashSet<Long>();
		ids.add(id);
		//this.variance = new float[dim];
		//this.M2 =  new float[dim];
		//this.sumvec = new float[dim];
		
		
	}
	private void updateVariance(float[] data){
				float delta;
	            count++;
	            
	    	    for(int i =0;i<data.length;i++){
	    	    		float x = data[i];
	    	            delta = x - vec[i];
	    	            //if(sumvec[i]==0 || delta<1.5){
	    	            	//sumvec[i]+=1;
		    	            vec[i] = vec[i] + delta/count;//sumvec[i];
		    	            //M2[i] = M2[i] + delta*(x - vec[i]);
		    	            //variance[i] = M2[i]/(sumvec[i]-1);
	    	            //}
	    	    }
	    	    
	}
	
	
	
	public float[] centroid()
	{
		//System.out.printf("%d\n",ids.size());
		return vec;
	}
//comment out to record variance
	public void updateVec(RPVector rp)
	{
		ids.addAll(rp.id);
		//vec = rp.data;
		updateVariance(rp.data);
	}
	
	public void updateVec(float[] rp)
	{

		updateVariance(rp);
	}
	public long getCount() {
		return count;
	}

}
