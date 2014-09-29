package uc.edu.rphash;

import java.util.Map;

import uc.edu.rphash.Readers.RPHashObject;
import uc.edu.rphash.decoders.Decoder;
import uc.edu.rphash.decoders.LeechDecoder;
import uc.edu.rphash.frequentItemSet.ItemSet;
import uc.edu.rphash.frequentItemSet.KarpFrequentItemSet;
import uc.edu.rphash.frequentItemSet.SimpleFrequentItemSet;
import uc.edu.rphash.lsh.LSH;
import uc.edu.rphash.projections.DBFriendlyProjection;
import uc.edu.rphash.projections.Projector;
import uc.edu.rphash.standardhash.FNVHash;
import uc.edu.rphash.standardhash.HashAlgorithm;

public class RPHash {
		
		Map<Long,Integer> map(RPHashObject so) {
			//create our LSH Machine
			
			HashAlgorithm hal = new FNVHash(so.hashmod);
			Decoder dec = new LeechDecoder();
			Projector p = new DBFriendlyProjection(so.dim, dec.getDimensionality(),so.randomseed);
			LSH lsh = new LSH(dec,p,hal);
			ItemSet<Long> is = new SimpleFrequentItemSet<Long>(so.k);
			
			int i;
			
			for (i =0;i<so.n;i++)
				//lossy add to frequent itemset the hashed Decoded randomly projected vector
				is.add(lsh.lshHash(so.getNextVector()));

			return is.getTop();

		}

		String reduce(String context) {

				return context ;

			}

}
