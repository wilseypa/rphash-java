package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.knn.cluster.StreamingKMeans;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.StreamClusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.tests.generators.GenerateStreamData;

public class DummyClusterer implements StreamClusterer {
	
	private RPHashObject so;
	List<float[]> data;
	List<Centroid> centroids;
	private int k;
	Random r;

	public DummyClusterer(RPHashObject streamObject) {

		this.so = streamObject;
		centroids = new ArrayList<Centroid>();
		this.k = streamObject.getk();
		this.r = new Random();
	}

	public DummyClusterer(int k, GenerateStreamData c, int processors) {
		this.so = new SimpleArrayReader(c, k);
		this.centroids = new ArrayList<Centroid>();
		this.k = so.getk();
		this.r = new Random();
	}

	@Override
	public long addVectorOnlineStep(float[] x) {
		
		if(centroids.size()<k*3){
			centroids.add(new Centroid(x));
		}else{
			if(r.nextInt(1000)==1){
				centroids.set(r.nextInt(k), new Centroid(x));
			}
		}

		return 0;
	}

	@Override
	public java.util.List<Centroid> getCentroidsOfflineStep() {

		Clusterer clusterer = so.getOfflineClusterer();
		clusterer.setData(centroids);
		clusterer.setK(this.k);
		
		return clusterer.getCentroids();

	}

	@Override
	public List<Centroid> getCentroids() {
		Clusterer clusterer = so.getOfflineClusterer();
		clusterer.setData(centroids);
		clusterer.setK(this.k);
		return clusterer.getCentroids();
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
