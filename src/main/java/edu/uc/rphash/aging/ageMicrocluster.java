package edu.uc.rphash.aging;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Readers.RPHashObject;



/*
1. set lambda ( default = 0.5) , set window size ( default = 1000 )
2. if starting then fill the windows with same data 5 times as 5 is our memory or we store last 5 results or last 5 set of top n microclusters.
3. assign weights to each set in a time decaying fashion. 
4. merge the weighted set of micro clusters. (multiple cases possible)
5. proceed with k-selection and get the final set of clusters/centroids.
5. compare with previous set of centroids and store difference.

*/

public class ageMicrocluster implements Runnable {

	
	static double decayRate = 0.5 ;
	static DecayPositional decay = new DecayPositional();

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}


	public static List< HashMap<Long, Long> > ageListOfMicroClusters(  List< HashMap<Long, Long> > Maps_OfIDAndCount ) {
		
		// HashMap<Long, Long> MapOfIDAndCount1 = new HashMap<>();
		for (int i = 0; i < Maps_OfIDAndCount.size(); i++) 
		    {
			
			double ageMultiplier= decay.ExpDecayFormula2 ( decayRate , i );
			HashMap<Long, Long> MapOfIDAndCount = Maps_OfIDAndCount.get(i);
			
			for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount.keySet())) {
				
				int cur_count = (int) (MapOfIDAndCount.get(cur_id).longValue());
			    
			    	cur_count = (int) (cur_count * ageMultiplier);
			    }
			}
	
		return Maps_OfIDAndCount;	
    }
}
