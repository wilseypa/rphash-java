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
	
}