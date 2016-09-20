package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

public class LloydIterativeKmeans implements Clusterer {
	int k;
	int n;
	List<float[]> data;
	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public List<float[]> getData() {
		return data;
	}
	
	
	@Override
	public void setRawData(List<float[]> data) {
		this.data = data;
	}
	
	@Override
	public void setData(List<Centroid> centroids) {
		ArrayList<float[]> data = new ArrayList<float[]>(centroids.size());
		for(Centroid c : centroids)data.add(c.centroid());
		setRawData(data);	
	}

	public List<Float> getWeights() {
		return weights;
	}

	public void setWeights(List<Float> weights) {
		this.weights = weights;
	}

	int projdim;

	List<float[]> means;
	List<List<Integer>> clusters;
	List<Float> weights;

	public LloydIterativeKmeans(int k, List<float[]> data) {
		this.k = k;
		this.data = data;
		this.projdim = 0;
		this.clusters = null;
		this.weights = new ArrayList<Float>(data.size());
		for (int i = 0; i < data.size(); i++)
			weights.add(1f);
		means = null;
	}

	public LloydIterativeKmeans(int k, List<float[]> data, List<Float> weights) {
		this.k = k;
		this.data = data;
		this.projdim = 0;
		this.clusters = null;
		this.weights = weights;
		means = null;
	}

	public LloydIterativeKmeans(int k, List<float[]> data, int projdim) {
		this.k = k;
		this.data = data;
		this.projdim = projdim;
		this.clusters = null;
		this.weights = new ArrayList<Float>(data.size());
		for (int i = 0; i < data.size(); i++)
			weights.add(1f);
		means = null;
	}

	public LloydIterativeKmeans() {
		// TODO Auto-generated constructor stub
	}

	public float[] computeCentroid(List<Integer> vectors, List<float[]> data) {
		int d = data.get(0).length;
		float[] centroid = new float[d];

		for (int i = 0; i < d; i++)
			centroid[i] = 0.0f;

		float w_total = 0f;
		for (Integer v : vectors) {
			w_total += weights.get(v);
		}

		for (Integer v : vectors) {
			float[] vec = data.get(v);
			float weight = (float) weights.get(v) / (float) w_total;
			for (int i = 0; i < d; i++)
				centroid[i] += (vec[i] * weight);
		}
		return centroid;
	}

	ArrayList<Integer> weightTotals;

	void updateMeans(List<float[]> data) {
		weightTotals = new ArrayList<Integer>();
		if (means == null) {
			means = new ArrayList<float[]>();
			for (int i = 0; i < k; i++)
				means.add(computeCentroid(clusters.get(i), data));
		}
		for (int i = 0; i < k; i++)
			means.set(i, computeCentroid(clusters.get(i), data));
	}

	int assignClusters(List<float[]> data) {
		int swaps = 0;
		List<List<Integer>> newClusters = new ArrayList<List<Integer>>();
		for (int j = 0; j < k; j++)
			newClusters.add(new ArrayList<Integer>());

		for (int clusterid = 0; clusterid < k; clusterid++) {

			for (Integer member : clusters.get(clusterid)) {

				int nearest = VectorUtil.findNearestDistance(data.get(member),
						means);
				newClusters.get(nearest).add(member);
				if (nearest != clusterid)
					swaps++;
			}

		}
		clusters = newClusters;
		return swaps;
	}

	private void run() {
		int maxiters = 1000;
		int swaps = 2;
		this.n = this.data.size();
		ArrayList<float[]> workingdata = new ArrayList<float[]>();
		// stuff for projected kmeans
		Projector p = null;
		Random r = new Random();
		if (projdim != 0)
			p = new DBFriendlyProjection(this.data.get(0).length, projdim,
					r.nextInt());
		for (float[] v : this.data) {
			if (p != null) {
				workingdata.add(p.project(v));
			} else
				workingdata.add(v);
		}
		
		int maxout = 0;
		//loop until there are no more nullsets
		boolean nullset = false;
		do {
			this.clusters = new ArrayList<List<Integer>>(k);
			// seed data with new clusters
			ArrayList<Integer> shufflelist = new ArrayList<Integer>(data.size());
			for (int i = 0; i < data.size(); i++)
				shufflelist.add(i);
			
			for (int i = 0; i < k; i++) {
				List<Integer> tmp = new LinkedList<Integer>();
				tmp.add(shufflelist.remove(0));
				
				for (int j = 1; j < workingdata.size() / k  ; j++) {
					int nxt = r.nextInt(shufflelist.size());
					tmp.add(shufflelist.remove(nxt));
				}
				this.clusters.add(tmp);
			}

	
			cluster(maxiters, swaps, n, workingdata, clusters);
			
			nullset = false;
			
			for (List<Integer> cluster : clusters) {
				nullset |= (cluster.size() == 0);
			}
			
		} while (nullset && ++maxout<100);
		if (maxout == 100)
			System.err.println("Warning: MaxIterations Reached Outer");

	}

	public void cluster(int maxiters, int swaps, int n,
			ArrayList<float[]> workingdata, List<List<Integer>> clusters) {
		while (swaps > 0 && maxiters > 0) {
			maxiters--;
			updateMeans(workingdata);
			swaps = assignClusters(workingdata);
		}
		if (maxiters == 0)
			System.err.println("Warning: MaxIterations Reached");
		updateMeans(this.data);
	}

	@Override
	public List<Centroid> getCentroids() {
		if (means == null)
			run();
		List<Centroid> centroids = new ArrayList<>(means.size());
		for(float[] f : means){
			centroids.add(new Centroid(f,0));
		}
		return centroids;
	}

	public static void main(String[] args) {
		GenerateData gen = new GenerateData(8, 100, 100);
		LloydIterativeKmeans kk = new LloydIterativeKmeans(5, gen.data(), 24);
//		VectorUtil.prettyPrint(kk.getCentroids());
	}

	@Override
	public RPHashObject getParam() {

		return new SimpleArrayReader(this.data, k);
	}


}
