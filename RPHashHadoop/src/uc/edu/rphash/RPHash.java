package uc.edu.rphash;

import java.util.Map;

import uc.edu.rphash.Readers.RPHashObject;

import uc.edu.rphash.decoders.Decoder;
import uc.edu.rphash.decoders.LeechDecoder;
import uc.edu.rphash.frequentItemSet.ItemSet;
import uc.edu.rphash.frequentItemSet.SimpleFrequentItemSet;
import uc.edu.rphash.lsh.LSH;
import uc.edu.rphash.projections.DBFriendlyProjection;
import uc.edu.rphash.projections.Projector;
import uc.edu.rphash.standardhash.FNVHash;
import uc.edu.rphash.standardhash.HashAlgorithm;


public class RPHash {
		
		Map<Long,Integer> map(RPHashObject so) {
			//create our LSH Machine
			HashAlgorithm hal = new FNVHash(so.getHashmod());
			Decoder dec = new LeechDecoder();
			Projector p = new DBFriendlyProjection(so.getdim(), 
					dec.getDimensionality(),so.getRandomSeed());
			LSH lsh = new LSH(dec,p,hal);
			ItemSet<Long> is = new SimpleFrequentItemSet<Long>(so.getk());

			//add to frequent itemset the hashed Decoded randomly projected vector
			for (int i =0;i<so.getn();i++)
				is.add(lsh.lshHash(so.getNextVector()));
			
			so.setIDs(is.getTop().keySet());
			so.reset();
			return is.getTop();
		}

		String reduce(String context) {
				return context ;
			}
		
		//not too fond of these functions /TODO but i'm sleepy
		void accumulateVectors(float[] centroid,float[] vec){
			for(int i =0; i < vec.length;i++)centroid[i]+=vec[i];
		}
		int findin(long o,long[] lst){
			for(int i =0;i<lst.length;i++)
				if(o == lst[i]){
					return i;}
			return -1;
		}
		
		
		public float[][] mapP2(RPHashObject so) {
			//create our LSH Machine
			HashAlgorithm hal = new FNVHash(so.getHashmod());
			Decoder dec = new LeechDecoder();
			Projector p = new DBFriendlyProjection(so.getdim(), 
					dec.getDimensionality(),so.getRandomSeed());
			LSH lsh = new LSH(dec,p,hal);
			float[][] centroids = new float[so.getk()][so.getdim()];
			int[] counts = new int[so.getk()];
			
			
			//add to frequent itemset the hashed Decoded randomly projected vector
			for (int i =0;i<so.getn();i++)
			{
				float[] vec = so.getNextVector();
				for (int j =0;j<Math.log(so.getn());j++)
				{
					long d = lsh.lshHash(vec);
					int loc = findin(d,so.getIDs());
					if(loc!=-1){
						accumulateVectors(centroids[loc], vec);
						counts[loc]++;
					}
				}
			}
			
			for (int i =0;i<so.getk();i++)
			{
				for (int j =0;j<so.getdim();j++){
					centroids[i][j] = centroids[i][j]/(float)counts[i];
				}
			}
			return centroids;
		}
		
		
		
		

}
