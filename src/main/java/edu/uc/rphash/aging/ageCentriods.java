package edu.uc.rphash.aging;

import java.util.List;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.aging.DecayPositional;


public class ageCentriods implements Runnable {
	
	static double decayRate = 0.5 ;
	static DecayPositional decay = new DecayPositional();

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	
	public static float[][] weighted_merge(double cnt_1, float[] x_1, 
			double cnt_2, float[] x_2) {
		
		
		cnt_1 = (float) cnt_1;
		cnt_2 = (float) cnt_2;
		
		
		float cnt_r = (float) (cnt_1 + cnt_2);
		float[] x_r = new float[x_1.length];
		
		for (int i = 0; i < x_1.length; i++) {
			x_r[i] = (float) ((cnt_1 * x_1[i] + cnt_2 * x_2[i]) / cnt_r);
				
		}

		float[][] ret = new float[3][];
		ret[0] = new float[1];
		
		ret[0][0] = cnt_r;
		ret[1] = x_r;
		return ret;
	}

	public static List<List<Centroid>> ageListOfcent(  List<List<Centroid>> prev ) {
		
	
		for (int i = 0; i < prev.size(); i++) 
		    {
			
			double ageMultiplier= decay.ExpDecayFormula2 ( decayRate , i );
			List<Centroid> tempCents = prev.get(i);
			
			for (int j =0 ; j<tempCents.size() ; j++) {
			    Centroid cent = tempCents.get(j);
			    
			    for (int k=0; k<cent.centroid.length ; k++ ) {
			    	cent.centroid[j]= (float) (cent.centroid[j] * ageMultiplier);
			    }
			}
	    }
	
		return prev;	
    }
	
	
	
	
	
	public static List<Centroid> ageListOfcents2(  List<Centroid> prev , List<Centroid> curr) {
		
		
		for (int i = 0; i < prev.size(); i++) 
		    {
			
			double ageMultiplier= decay.ExpDecayFormula2 ( decayRate , i );
			List<Centroid> tempCents = (List<Centroid>) prev.get(i);
			
			for (int j =0 ; j<tempCents.size() ; j++) {
			    Centroid cent = tempCents.get(j);
			    
			    for (int k=0; k<cent.centroid.length ; k++ ) {
			    	cent.centroid[j]= (float) (cent.centroid[j] * ageMultiplier);
			    }
			}
	    }
	
		return prev;	
    }
}