package edu.uc.rphash.lsh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

/** LSH-kNN , uses leech lattice decoder by default
 * Next Steps:
 *  Allow for other LSH's
 *  Allow for hash concatenation for LSH's that produce shorter keys
 * @author lee
 *
 */
public class LSHkNN {
	
	int d;
	int l; 
	LSH[] hasher;
	HashMap<Long,List<float[]>>[] bins; 
	Random r;

	/**
	 * @param d - the dimensionality of vectors to expect
	 * @param L - the number of tables to create (more = more accurate but slower)
	 */
	public LSHkNN(int d, int L) {
		this.d = d;
		this.l=L;
		this.bins =new HashMap[this.l];
		this.hasher = new LSH[this.l];
		this.r = new Random();
		for(int i = 0;i<this.l;i++)
		{
			bins[i] =  new HashMap<Long,List<float[]>>();
			this.hasher[i] = new LSH(d,r.nextLong(),false);
		}
	}
		


		/** Create the tables necessary for an LSH accelerated k - nearest neighbor search
		 * @param points
		 */
		void createDB(List<float[]> points) {
			int nPoints = points.size();
			
			for (int j = 0; j < nPoints; j++) {
				float[] p = points.get(j);

				for (int i = 0; i < this.l; i++) {
					long hashcode = this.hasher[i].lshHash(p);
					if(this.bins[i].containsKey(hashcode)){
						this.bins[i].get(hashcode).add(p);
					}else{
						List<float[]> tmp = new ArrayList<>();
						tmp.add(p);
						this.bins[i].put(hashcode,tmp);
					}
					
				}
			}
		}


		/** This method queries the LSH tables for vectors near p
		 * Uses random probe sampling near the vector query vector
		 * to cover sparseness of the Leech Lattice. uses up to 
		 * log_2(1/leech density) ~ 9 probes
		 * @param p
		 * @return list of near vectors
		 */
		public List<float[]> queryDB(float[] p) {

			List<float[]> neighborhood = new ArrayList<>();
			for (int i = 0; i < this.l; i++) {
				long hashcode = this.hasher[i].lshHash(p);
				List<float[]> tmpnbhd = bins[i].get(hashcode);
				int tries = 0;
				while(tmpnbhd==null && tries<9)
				{
					float[] phat = new float[p.length];
					
					for(int j = 0;j<phat.length;j++)
						phat[j] = p[j] + (float)r.nextGaussian();
					
					hashcode = this.hasher[i].lshHash(phat);
					tmpnbhd = bins[i].get(hashcode);
					tries++;
				}
				neighborhood.addAll(tmpnbhd);
			}
			return neighborhood;
		}
		
		
		/** This method queries an existing lsh db and truncates to the  k nearest neighbors using an LSH filter
		 * followed by a linear search
		 * search
		 * @param k
		 * @param p
		 * @return the k nearest vectors
		 */
		public List<float[]> knn(int k,float[] p){
			return linearknn(k,p,queryDB(p));
		}
		
		
		/** Linear search for the k nearest vectors from a list of vectors to the query point p
		 * @param k
		 * @param p
		 * @param db
		 * @return the k nearest vectors
		 */
		public List<float[]> linearknn(int k,float[] p,List<float[]> db){
			
			class DistAndVec implements Comparable<DistAndVec>
			{
				public float[] q;
				public float dist;
				public DistAndVec(float[] q)
				{
					this.q = q;
					this.dist = 0.0f;
					for(int i =0;i<q.length;i++)dist+=(q[i]-p[i])*(q[i]-p[i]);
					dist = (float) Math.sqrt(dist);
					
				}
				@Override
				public int compareTo(DistAndVec o) {
					if(dist - o.dist > 0)return 1;
					if(dist - o.dist < 0)return -1;
					return 0;
				}
			};
			
			TreeSet<DistAndVec> qlist = new TreeSet<>();
			for(float[] q : db){
				qlist.add(new DistAndVec(q));
			}
			
			
			List<float[]> ret = new ArrayList<>(k);
			for(int i = 0;i<k && i<qlist.size();i++){
				DistAndVec dv = qlist.pollFirst();
				ret.add(dv.q);
			}
			return ret;
		}

		
		public static void main(String[] args){
			GenerateData g = new GenerateData(10,1000,100);
			LSHkNN querier = new LSHkNN(100, 5);
			querier.createDB(g.data);
			
			VectorUtil.prettyPrint(querier.knn(10, g.data.get(new Random().nextInt(g.data.size()))));
			
			
			
			
		}


}
