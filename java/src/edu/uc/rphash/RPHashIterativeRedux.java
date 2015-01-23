package edu.uc.rphash;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.RPVector;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.LeechDecoder;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;
import edu.uc.rphash.frequentItemSet.StickyWrapper;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.standardhash.NoHash;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

/**This is the Iterative Redux Version Of RPHash
 * Instead of a constant number of passes over the data as in RPHash
 * the Redux variant does k-passes over the data, finding the max
 * density mode at each pass, up to k density modes. 
 * @author lee
 */
public class RPHashIterativeRedux  implements Clusterer
{
	float variance;
	Random r  = new Random();
	public RPHashObject map(RPHashObject so) 
	{
		Random r = new Random();
		HashAlgorithm hal = new NoHash();
		Iterator<RPVector> vecs = so.getVectorIterator();
		if(!vecs.hasNext())return so;
		
		Projector p = new DBFriendlyProjection(so.getdim(),
				LeechDecoder.Dim,  r.nextInt());
		Decoder dec = new LeechDecoder(variance/1.25f);
		LSH lshfunc = new LSH(dec, p, hal);
		//(int)(Math.log(so.getk())*so.getk()+.5);
		//
		long[] hash;
		int probes = 3;
		int k =so.getk()*probes;
		ItemSet<Long> is = new SimpleFrequentItemSet<Long>(k);
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vecs.hasNext()) {
			RPVector vec = vecs.next();
		    hash = lshfunc.lshHashRadius(vec.data,probes);
			for (int j=0; j < probes;j++) {
				is.add(hash[j]);
			    vec.id.add(hash[j]);
			}
		}
		so.setPreviousTopID(is.getTop());
		//for(Long l : is.getCounts())System.out.printf("%d,",l);System.out.printf("\n,");
		return so;

	}
	
	class Tuple implements Comparable<Tuple>{
		public float dist;
		public RPVector vec;
		public Tuple(RPVector next, float distance) {
			this.dist=distance;
			this.vec = next;
		}
		@Override
		public int compareTo(Tuple o) {
			return this.dist < o.dist?0:1;
		}

	}
	
	
	public RPHashObject reduce(RPHashObject so) 
	{
		Long lastID = so.getPreviousTopID().get(0);
		Centroid centroid = new Centroid(so.getdim(),lastID);

		
		Iterator<RPVector> vecs = so.getVectorIterator();
		RPVector vec;
		
//		while(vecs.hasNext())
//		{
//			vec = vecs.next();
//			if(vec.id.contains(lastID))
//			{
//				centroid.updateVec(vec);
//				vecs.remove();
//			}
//		}

		int ct = 0;
		//sort and remove top n/k items		
		vecs =  so.getVectorIterator();
		ArrayList<Tuple> pq = new ArrayList<Tuple>();
		while(vecs.hasNext())
		{
			vec = vecs.next();
			pq.add(new Tuple(vec,TestUtil.distance(vec.data,centroid.centroid())));
		}
		
		Collections.sort(pq);
		float cutoff = pq.get(pq.size()/(so.getk())).dist;
		//System.out.println(pq.get(pq.size()/so.getk()).dist;);
		vecs =  so.getVectorIterator();
		
		while(vecs.hasNext())
		{
				vec = vecs.next();
				if(TestUtil.distance(vec.data,centroid.centroid()) < cutoff){
					ct++;
					for(int d = 0 ; d<so.getdim();d++)centroid.centroid()[d]=( centroid.centroid()[d]*ct++ + vec.data[d])/(float)ct;
					vecs.remove();
				}
		}	
		
		so.addCentroid(centroid.centroid());
		
		return so;
	}
	
	
	
	
	private List<float[]> centroids=null;
	private RPHashObject so;
	public RPHashIterativeRedux (List<float[]> data,int k){
		variance = StatTests.varianceAll(data);
		so = new SimpleArrayReader(data,k,1,250000);
	}
	public RPHashIterativeRedux (RPHashObject so){
		this.so= so;
	}
	public RPHashIterativeRedux (List<float[]> data,int k,int rseed){
		variance = StatTests.varianceAll(data);
		so = new SimpleArrayReader(data,k,rseed,250000);
	}

	
	public List<float[]> getCentroids(RPHashObject so){
		if(centroids == null)run(so);
		return centroids;
	}
	
	@Override
	public List<float[]> getCentroids(){
		
		if(centroids == null)run(so);
		return centroids;
	}
	
	private  void run(RPHashObject so)
	{
		for(int i = 0;i<so.getk();i++)
		{
			so = map(so);
			so = reduce(so);

		}
		centroids = so.getCentroids();
	}

	
	public static void main(String[] args){
		
		int k = 20;
		int d = 5000;
		int n = 10000;
		GenerateData gen = new GenerateData(k,n/k,d,1.f,true,1.f);
		
		RPHashObject sar = new SimpleArrayReader(gen.data(),k,1,250000);
		RPHashIterativeRedux rphit = new RPHashIterativeRedux(sar);
		
		long startTime = System.nanoTime();

		long duration = (System.nanoTime() - startTime);
		List<float[]> aligned  = TestUtil.alignCentroids(rphit.getCentroids(),gen.medoids());
		System.out.print(StatTests.PR(aligned,gen)+":"+duration/1000000000f);
		System.out.print("\n");
		System.gc();
		
	}

}
