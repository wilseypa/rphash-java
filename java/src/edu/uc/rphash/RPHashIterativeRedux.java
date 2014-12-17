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
import edu.uc.rphash.frequentItemSet.StickyWrapper;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

/**This is the Iterative Redux Version Of RPHash
 * Instead of a constant number of passes over the data as in RPHash
 * the Redux variant does k-passes over the data, finding the max
 * density mode at each pass, up to k density modes. 
 * @author lee
 */
public class RPHashIterativeRedux 
{
	
	Random r  = new Random();
	public RPHashObject map(RPHashObject so) 
	{
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		Iterator<RPVector> vecs = so.getVectorIterator();
		if(!vecs.hasNext())return so;
		
		RPVector vec = vecs.next();
		
		float radius = 1.0f;//TestUtil.max(vec.data);
		
		Decoder dec = new LeechDecoder(radius);
		Projector p ;
		
		//for (int i = 0; i < so.getTimes(); i++) {
			p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextInt());
		//}
		LSH lsh = new LSH(dec, p, hal);
		ItemSet<Long> is = new StickyWrapper<Long>(so.getk(), so.getn());
		long hash;
		int probes = (int) (Math.log(so.getn()) + .5);
		
		
		while(vecs.hasNext())
		{
			hash = lsh.lshHash(vec.data);
			is.add(hash);
			vec.id.add(hash);
			
//			for (int j=0; j < probes;j++) {
//				hash = lsh.lshHashRadius(vec.data);
//				is.add(hash);
//			    vec.id.add(hash);
//			}
			vec = vecs.next();
			
		}
		so.setPreviousTopID(is.getTop());

		return so;
	}
	
//	class Tuple implements Comparable<Tuple>{
//		public float dist;
//		public RPVector vec;
//		public Tuple(RPVector next, float distance) {
//			this.dist=distance;
//			this.vec = next;
//		}
//		@Override
//		public int compareTo(Tuple o) {
//			return this.dist < o.dist?0:1;
//		}
//
//	}
	
	
	public RPHashObject reduce(RPHashObject so) 
	{
		float[] centroid = new float[so.getdim()];
		int ct = 0;
		
		Iterator<RPVector> vecs = so.getVectorIterator();
		RPVector vec;

		while(vecs.hasNext())
		{
			vec = vecs.next();
			if(vec.id.contains(so.getPreviousTopID().get(0)))
			{
				ct++;
				for(int d = 0 ; d<so.getdim();d++)centroid[d]+=vec.data[d];
				vecs.remove();
			}
		}

		float ctinv = 1.0f/(float)ct;
		for(int d = 0 ; d<so.getdim();d++)centroid[d]*=ctinv;
		
//		sort and remove top n/k items		
//		vecs =  so.getVectorIterator();
//		ArrayList<Tuple> pq = new ArrayList<Tuple>();
//		while(vecs.hasNext())
//		{
//			vec = vecs.next();
//			pq.add(new Tuple(vec,TestUtil.distance(vec.data,centroid)));
//		}
//		
//		Collections.sort(pq);
//		float cutoff = pq.get(pq.size()/(so.getk()*100)).dist;
//		//System.out.println(pq.get(pq.size()/so.getk()).dist;);
//		vecs =  so.getVectorIterator();
//		
//		while(vecs.hasNext())
//		{
//				vec = vecs.next();
//				if(TestUtil.distance(vec.data,centroid) < cutoff){
//					ct++;
//					for(int d = 0 ; d<so.getdim();d++)centroid[d]=( centroid[d]*ct++ + vec.data[d])/(float)ct;
//					vecs.remove();
//				}
//		}	
		
		so.addCentroid(centroid);
		
		return so;
	}
	
	
	
	
	private List<float[]> centroids=null;
	private RPHashObject so;
	public RPHashIterativeRedux (List<float[]> data,int k){
		so = new SimpleArrayReader(data,k,1,250000,1);
	}
	public RPHashIterativeRedux (RPHashObject so){
		this.so= so;
	}
	public RPHashIterativeRedux (List<float[]> data,int k,int rseed){
		so = new SimpleArrayReader(data,k,rseed,250000,1);
	}

	
	public List<float[]> getCentroids(RPHashObject so){
		if(centroids == null)run(so);
		return centroids;
	}
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
		int d = 1000;
		int n = 50000;
		GenerateData gen = new GenerateData(k,n/k,d,1.0f,true,1.f);
		
		RPHashObject sar = new SimpleArrayReader(gen.data(),k,1,250000,1);
		RPHashIterativeRedux rphit = new RPHashIterativeRedux(sar);
		
		long startTime = System.nanoTime();

		long duration = (System.nanoTime() - startTime);
		List<float[]> aligned  = TestUtil.alignCentroids(rphit.getCentroids(),gen.medoids());
		System.out.print(StatTests.PR(aligned,gen)+":"+duration/1000000000f);
		System.out.print("\n");
		System.gc();
		
	}

}
