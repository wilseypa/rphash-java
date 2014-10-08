package edu.uc.rphash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.LeechDecoder;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;
import edu.uc.rphash.frequentItemSet.StickyWrapper;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.FNVHash;
import edu.uc.rphash.standardhash.HashAlgorithm;


public class RPHash {
		
		public RPHashObject mapP1(RPHashObject so) {
			//create our LSH Machine
			HashAlgorithm hal = new FNVHash(so.getHashmod());
			Decoder dec = new LeechDecoder();
			Projector p = new DBFriendlyProjection(so.getdim(), 
					dec.getDimensionality(),so.getRandomSeed());
			LSH lsh = new LSH(dec,p,hal);
			ItemSet<Long> is = new StickyWrapper<Long>(so.getk());

			//add to frequent itemset the hashed Decoded randomly projected vector
			for (int i =0;i<so.getn();i++)
				is.add(lsh.lshHash(so.getNextVector()));
	
			so.setIDs(is.getTop());
			so.setCounts(is.getCounts());
			so.reset();
			
			return so;
		}

		public String reduceP1(String context) {
				return context ;
			}
		
		
		public RPHashObject mapP2(RPHashObject so) {
			//create our LSH Machine
			HashAlgorithm hal = new FNVHash(so.getHashmod());
			Decoder dec = new LeechDecoder();
			Projector p = new DBFriendlyProjection(so.getdim(), 
					dec.getDimensionality(),so.getRandomSeed());
			LSH lsh = new LSH(dec,p,hal);

			//make a set of k default centroid objects
			HashMap<Long,Centroid> centroids = new HashMap<Long,Centroid>();
			for(Long id:so.getIDs())
				centroids.put(id, new Centroid(so.getdim()) );
			//start the calculation
			long d;
			float[] vec = so.getNextVector();
			
			//add to frequent itemset the hashed Decoded randomly projected vector
			while(vec != null)
			{
				int j = 0 ;
				Centroid cent = null;
				do{
					d = lsh.lshHash(vec);
					cent = centroids.get(d);
					if(cent!=null)cent.updateVec(vec);
					
				}while(cent ==null && j++<Math.log(so.getn()));
				vec = so.getNextVector();
			}
			
			
			for (Long id: centroids.keySet()){
				so.addCentroid(centroids.get(id).centroid());
			}
			so.reset();
			return so;
		}

		public RPHashObject reduceP2(RPHashObject so){
			return so;
		}
		
		
		
		

}
