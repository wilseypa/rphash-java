package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.knn.cluster.StreamingKMeans;
import org.apache.mahout.knn.search.UpdatableSearcher;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.RPHashStream;
import edu.uc.rphash.StreamClusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.generators.GenerateStreamData;

public class StreamingKmeans2 implements StreamClusterer {
	private RPHashObject so;
	int processors;
	List<float[]> data;
	List<float[]> centroids;
	int k;
	StreamingKMeans clusterer;
	private UpdatableSearcher searcher;

	public StreamingKmeans2(RPHashObject streamObject) {

		searcher = new org.apache.mahout.knn.search.LocalitySensitiveHashSearch(new EuclideanDistanceMeasure(), 10);
		this.so = streamObject;
		this.clusterer = new StreamingKMeans(searcher, 1 << so.getdim(), 0.001D);
		centroids = null;
		this.k = streamObject.getk();

	}

	public StreamingKmeans2(int k, GenerateStreamData c, int processors) {
		this.so = new SimpleArrayReader(c, k);
		this.centroids = null;
		this.k = so.getk();
		searcher = new org.apache.mahout.knn.search.LocalitySensitiveHashSearch(new EuclideanDistanceMeasure(), 10);
		this.clusterer = new StreamingKMeans(searcher, 1 << so.getdim(), 0.001D);

	}

	@Override
	public long addVectorOnlineStep(float[] x) {
		Vector v = new DenseVector(x.length);
		for (int i = 0; i < x.length; i++)
			v.setQuick(i, x[i]);
		org.apache.mahout.math.Centroid c = new org.apache.mahout.math.Centroid(
				1, v);

		clusterer.cluster(c);
		return 0;
	}

	@Override
	public java.util.List<Centroid> getCentroidsOfflineStep() {
		
		
		ArrayList<Centroid> cents = new ArrayList<>();
		for (org.apache.mahout.math.Centroid c : clusterer
				.getCentroidsIterable()) {

			float[] flt = new float[c.size()];

			for (int i = 0; i < c.size(); i++)
				flt[i] = (float) c.getQuick(i);

			cents.add(new Centroid(flt));
		}
		
		Clusterer clusterer = so.getOfflineClusterer();
		clusterer.setData(cents);
		clusterer.setK(this.k);
		
		
		
		return clusterer.getCentroids();

	}

	@Override
	public List<Centroid> getCentroids() {
		ArrayList<Centroid> cents = new ArrayList<>();
		for (float[] c : centroids) {
			cents.add(new Centroid(c));
		}
		return cents;
	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

	@Override
	public void setWeights(List<Float> counts) {

	}

	@Override
	public void setRawData(List<float[]> centroids) {
		this.data = centroids;
	}

	@Override
	public void setData(List<Centroid> centroids) {
		data = new ArrayList<>();
		for (Centroid c : centroids)
			this.data.add(c.centroid);
	}

	@Override
	public void setK(int getk) {
		this.k = getk;

	}

	@Override
	public void reset(int randomseed) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setMultiRun(int runs) {
		return false;
	}

	@Override
	public void shutdown() {

	}

	@Override
	public int getProcessors() {
		return 0;
	}

}
