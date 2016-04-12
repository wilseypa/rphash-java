package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

public class Agglomerative2 implements Clusterer {
	private class DistAndVector implements Comparable<DistAndVector> {
		Float dist;
		Integer vec;

		@Override
		public int compareTo(DistAndVector o) {
			// if (equals(o))
			// return 0;
			if (dist.floatValue() == o.dist.floatValue())
				return 1;
			if (dist.floatValue() < o.dist.floatValue())
				return -1;
			return 1;

		}
	}

	private class PQAndVector implements Comparable<PQAndVector> {
		TreeSet<DistAndVector> pq;
		Integer vec;

		PQAndVector(Integer vec) {
			this.pq = new TreeSet<DistAndVector>();
			this.vec = vec;
		}

		// @Override
		// public boolean equals(Object o) {
		//
		// return ((PQAndVector) o).vec.intValue() == vec.intValue();
		// }

		@Override
		public int compareTo(PQAndVector r) {

			// if (equals(r)){
			// System.out.println("whatthehell"+ r.vec.intValue() + ":"+
			// this.vec.intValue());
			// return 0;
			//
			// }
			if (pq.isEmpty()) {
				return 1;
			}
			if (r.pq.isEmpty()) {
				return -1;
			}

			if (pq.first().dist == r.pq.first().dist) {
				return 1;
			}
			return pq.first().compareTo(r.pq.first());
		}
	}

	int k;
	TreeSet<PQAndVector> outerpq = new TreeSet<PQAndVector>();
	List<float[]> data;
	float counts[];

	private void distanceArray(List<float[]> data) {
		int n = data.size();
		for (int i = 0; i < n - 1; i++) {
			PQAndVector innerpq = new PQAndVector(new Integer(i));
			
			for (int j = i + 1; j < n; j++) {
				DistAndVector dv = new DistAndVector();
				dv.dist = new Float(VectorUtil.distance(data.get(i),
						data.get(j)));
				dv.vec = new Integer(j);
				innerpq.pq.add(dv);
			}
			//
			// System.out.print(i+" : ");
			// for(Object p:
			// innerpq.pq.toArray())System.out.print(((DistAndVector)p).vec+", ");System.out.println();
			outerpq.add(innerpq);
		}
		// for(PQAndVector p:
		// outerpq)System.out.print(p.vec+", ");System.out.println();

	}
	
	private void distanceArray2(List<float[]> data2,List<Integer> projIDs) {
		int n = data.size();
		for (int i = 0; i < n - 1; i++) {
			PQAndVector innerpq = new PQAndVector(new Integer(i));
			
			for (int j = i + 1; j < n; j++) {
				DistAndVector dv = new DistAndVector();
				if(projIDs.get(i).equals(projIDs.get(j))){
					dv.dist = Float.MAX_VALUE;
				}
				else{
					dv.dist = new Float(VectorUtil.distance(data.get(i),
							data.get(j)));
				}

				
				dv.vec = new Integer(j);
				innerpq.pq.add(dv);
			}
			outerpq.add(innerpq);
		}	
	}

	private void mergeAndUpdateCentroids(int newdata, int olddata) 
	{
		float[] u = data.get(newdata);
		float[] v = data.get(olddata);
		float ct1 = counts[newdata];
		float ct2 = counts[olddata];
		float[] w = new float[u.length];
		for (int i = 0; i < u.length; i++)
			w[i] = (u[i] * ct1 + v[i] * ct2) / (ct1 + ct2);
		counts[newdata] += counts[olddata];
		data.set(newdata, w);

	}

	/**
	 * remove the next two nearest vectors and perform a counts weighted average
	 * of the vectors. put this vector in the lower of the two vector indeces.
	 */
	private void merge() {
		// pop the queue with the nearest top vector in it

		PQAndVector innerpq = outerpq.pollFirst();
		//lower id lists are not checked for removals, check here.
		while (innerpq.pq.isEmpty()) {
			innerpq = outerpq.pollFirst();
		}
		// pop the nearest vector
		DistAndVector dv = innerpq.pq.pollFirst();

		int newvecloc = innerpq.vec;
		int olddata = dv.vec;

		Iterator<PQAndVector> it = outerpq.iterator();
		while (it.hasNext()) {
			PQAndVector v = it.next();
			if (v.vec.intValue() == olddata) {
				
				it.remove();
				break;
			}
		}

		// merge the two vectors
		mergeAndUpdateCentroids(newvecloc, olddata);

		PQAndVector newpq = new PQAndVector(newvecloc);

		Iterator<PQAndVector> pqit = outerpq.iterator();
		while (pqit.hasNext()) {
			PQAndVector itpq = pqit.next();

			// remove the merged vectors from all inner lists
			Iterator<DistAndVector> itdv = itpq.pq.iterator();
			while (itdv.hasNext()) {
				DistAndVector v = itdv.next();
				if (v.vec.intValue() == newvecloc) {
					itdv.remove();
					break;
				}
			}
			
			itdv = itpq.pq.iterator();
			while (itdv.hasNext()) {
				DistAndVector v = itdv.next();
				if ( v.vec.intValue() == olddata) {
					itdv.remove();
					break;
				}
			}
		}

		// System.out.println("lists after removal");
		// printlists();

		pqit = outerpq.iterator();
		while (pqit.hasNext()) {
			PQAndVector itpq = pqit.next();
			// add the new vector distance to all upper parent lists
			// compute new distance to vecs who have this vector in their list
			if (itpq.vec < newvecloc) {
				DistAndVector dv3 = new DistAndVector();
				dv3.dist = new Float(VectorUtil.distance(data.get(newvecloc),
						data.get(itpq.vec)));

				dv3.vec = new Integer(newvecloc);

				// add the updated vector to the new lists
				itpq.pq.add(dv3);
			}
		}

		// System.out.println("lists after adding back into lower idx lists");
		// printlists();

		pqit = outerpq.iterator();
		while (pqit.hasNext()) {
			PQAndVector itpq = pqit.next();
			// add to the new merge list
			if (itpq.vec > newvecloc) {
				DistAndVector dv2 = new DistAndVector();
				dv2.dist = new Float(VectorUtil.distance(data.get(newvecloc),
						data.get(itpq.vec)));
				dv2.vec = new Integer(itpq.vec);
				newpq.pq.add(dv2);
			}
		}

		outerpq.add(newpq);

		// System.out.println("adding merged list back");
		// printlists();

	}

	private void printlists() {
		System.out.println();
		for (Object o : outerpq.toArray()) {
			System.out.print("\t" + ((PQAndVector) o).vec + " : ");
			for (Object p : ((PQAndVector) o).pq.toArray()) {
				System.out.print(((DistAndVector) p).vec + ", ");
			}
			System.out.println();
		}
	}

	private void run() {
		while (outerpq.size() > k) {

			merge();
		}
		Iterator<PQAndVector> pqit = outerpq.iterator();
		centroids = new ArrayList<float[]>();
		while (pqit.hasNext()) {
			PQAndVector innerpq = pqit.next();
			centroids.add(data.get(innerpq.vec));
		}
	}

	public static void main(String[] args) {

		for (int i = 0; i < 1000; i += 10) {
			long avgtime = 0;
			float avgdistagg = 0;
			float avgdistreal = 0;
			float avgdistkm = 0;
			if(i!=0){
			for (int j = 0; j < 5; j++) {
				GenerateData gen = new GenerateData(10, i, 10, .5f);
				List<float[]> data = gen.data;

				long timestart = System.currentTimeMillis();
				Clusterer km1 = new Kmeans(10, data);
				Clusterer ag1 = new Agglomerative2(10, data);
				avgdistagg+=StatTests.WCSSE(ag1.getCentroids(), data);
				avgdistkm+=StatTests.WCSSE(km1.getCentroids(), data);
				avgdistreal+=StatTests.WCSSE(gen.getMedoids(), data);
				avgtime += (System.currentTimeMillis() - timestart);
			}
			}
			System.out.println(i + "\t" + avgtime / 5+"\t"+avgdistagg/5f+"\t"+avgdistkm/5f+"\t"+avgdistreal/5f);
		}

	}

	List<float[]> centroids;

	@Override
	public List<float[]> getCentroids() {

		if (centroids == null)
			run();
		return centroids;
	}

	@Override
	public RPHashObject getParam() {
		return null;
	}

	public void printDistanceArray() {
		for (int i = 0; i < data.size(); i++) {
			for (int j = 0; j < data.size(); j++)
				System.out.printf("%.2f,",
						VectorUtil.distance(data.get(i), data.get(j)));
			System.out.println();
		}
		System.out.println();
	}

	public Agglomerative2(int k, List<float[]> data) {
		this.k = k;
		this.data = data;
		this.counts = new float[data.size()];
		for (int i = 0; i < counts.length; i++)
			counts[i] = 1;
		
		distanceArray(data);

	}

	public Agglomerative2(int k, List<float[]> data, List<Float> counts) {
		this.k = k;
		this.data = data;
		this.counts = new float[counts.size()];
		for (int i = 0; i < counts.size(); i++)
			this.counts[i] = counts.get(i);
		distanceArray(data);
	}
	
	public Agglomerative2(int k, List<float[]> data, List<Float> counts,List<Integer> projectionIDs) {
		this.k = k;
		this.data = data;
		this.counts = new float[counts.size()];
		for (int i = 0; i < counts.size(); i++)
			this.counts[i] = counts.get(i);
		distanceArray2(data,projectionIDs);
	}
	
	@Override
	public void setWeights(List<Float> counts) {
		
	}

	@Override
	public void setData(List<float[]> centroids) {
		this.data = centroids;
		
	}

	@Override
	public void setK(int getk) {
		this.k = getk;
	}
	


}
