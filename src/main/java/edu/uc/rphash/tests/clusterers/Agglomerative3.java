package edu.uc.rphash.tests.clusterers;

import java.util.List;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.tests.clusterers.Agglomerative3.ClusteringType;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

public class Agglomerative3 implements Clusterer {
	/**
	 * 
	 * 
	 * @author betorfs
	 */
	class SuperCluster extends Cluster {

		private List<Cluster> subClusters = new ArrayList<>();

		public SuperCluster() {

		}

		public SuperCluster(Cluster first, Cluster second) {
			subClusters.add(first);
			subClusters.add(second);
		}

		public List<SimpleItem> getItems() {
			List<SimpleItem> result = new ArrayList<>();
			for (Cluster subCluster : subClusters) {
				result.addAll(subCluster.getItems());
			}
			return result;
		}

		public List<Cluster> getSubClusters() {
			return subClusters;
		}

		public double meanInterdistance() {
			List<SimpleItem> items = getItems();
			Mean mean = new Mean();
			for (int i = 0; i < items.size(); i++) {
				for (int j = i + 1; j < items.size(); j++) {
					double distance = items.get(i).distanceTo(items.get(j));
					mean.increment(distance);
				}
			}
			return mean.getResult();
		}
	}

	abstract class Cluster {

		public abstract List<SimpleItem> getItems();

		public List<SimpleItem> getValue() {
			List<SimpleItem> ret = new ArrayList<SimpleItem>();
			for (Cluster subCluster : getSubClusters()) {
				ret.addAll(subCluster.getValue());
			}

			return ret;
		}

		public abstract List<Cluster> getSubClusters();

		public double singleLinkageDistance(Cluster other) {
			double shortestDistance = Double.MAX_VALUE;

			for (SimpleItem item : getItems()) {
				for (SimpleItem otherItem : other.getItems()) {
					double distance = item.distanceTo(otherItem);
					if (distance < shortestDistance) {
						shortestDistance = distance;
					}
				}
			}

			return shortestDistance;
		}

		public double completeLinkageDistance(Cluster other) {
			double longestDistance = Double.MIN_VALUE;

			for (SimpleItem item : getItems()) {
				for (SimpleItem otherItem : other.getItems()) {
					double distance = item.distanceTo(otherItem);
					if (distance > longestDistance) {
						longestDistance = distance;
					}
				}
			}

			return longestDistance;
		}

		public double averageLinkageDistance(Cluster other) {
			double totalDistance = 0;
			int nbOfDistances = 0;

			for (SimpleItem item : getItems()) {
				for (SimpleItem otherItem : other.getItems()) {
					double distance = item.distanceTo(otherItem);
					totalDistance = totalDistance + distance;
					nbOfDistances++;
				}
			}

			return (totalDistance / nbOfDistances);
		}

		public abstract double meanInterdistance();

		@Override
		public String toString() {
			if (this.getValue() != null) {
				return this.getValue().toString();
			} else {
				StringBuilder sbt = new StringBuilder();
				for (Cluster subCluster : getSubClusters()) {
					sbt.append(subCluster.toString() + ",");
				}
				return sbt.substring(0, sbt.length() - 1).toString();
			}
		}
	}

	class SimpleItem extends Cluster {
		public List<SimpleItem> getItems() {
			ArrayList<SimpleItem> result = new ArrayList<>();
			result.add(this);
			return result;
		}

		public List<Cluster> getSubClusters() {
			return new ArrayList<>();
		}

		@Override
		public String toString() {
			return Arrays.toString(value);

		}

		public double meanInterdistance() {
			return 0;
		}

		private float[] value;
		private float weight;

		public SimpleItem(float[] value, float weight) {
			this.value = value;
			this.weight = weight;
		}

		public float distanceTo(SimpleItem other) {
			if (other instanceof SimpleItem
					&& other.value.length == this.value.length) {
				float sum = 0.0f;
				for (int i = 0; i < other.value.length; i++)
					sum += ((other.value[i] - value[i]) * (other.value[i] - value[i]));
				return (float) Math.sqrt(sum);
			} else {
				return Float.MAX_VALUE;
			}
		}

		@Override
		public List<SimpleItem> getValue() {
			List<SimpleItem> ret = new ArrayList<SimpleItem>();
			ret.add(this);
			return ret;
		}

		public void setValue(float[] value) {
			this.value = value;
		}

		public float getWeight() {
			return weight;
		}
	}
	
	private static class ClusterPair {
		public Cluster first;
		public Cluster second;

		public ClusterPair(Cluster first, Cluster second) {
			this.first = first;
			this.second = second;
		}
	}
	
	/**
	 * Agglomerative clustering algorithm. Continually merges the two most
	 * similar clusters in the input list that are the most similar into a
	 * bigger cluster. The input list are clusters of size 1
	 * 
	 * @author betorfs
	 */

	private ClusteringType type;
	private List<Float> weights;

	/**
	 * Creates a agglomerative clustering algorithm using the given clustering
	 * type: average linkage (minimizes the average distance between elements),
	 * complete (minimizes the largest distance between elements), or single
	 * linkage (minimizes the shortest distance between elements).
	 * 
	 * @param type
	 *            The type of clustering to use.
	 */
	public Agglomerative3(ClusteringType type,List<float[]> data, int k) {
		this.type = type;
		this.data = data;
		this.k = k;
	}
	
	public Agglomerative3(List<float[]> data, int k) {
		this.type = ClusteringType.SINGLE_LINKAGE;
		this.data = data;
		this.k = k;
	}
	
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
	public void setData(List<Centroid> centroids) {
		this.data = new ArrayList<float[]>(centroids.size());
		for(Centroid c : centroids) data.add(c.centroid());
	}
	@Override
	public void setRawData(List<float[]> centroids) {
		this.data = centroids;
	}
	
	public List<Float> getWeights() {
		return weights;
	}

	public void setWeights(List<Float> weights) {
		this.weights = weights;
	}

	public Agglomerative3(ClusteringType singleLinkage) {
		type = singleLinkage;
	}

	public List<Cluster> induceClusters(Collection<SimpleItem> items, int breakK) {
		// Create initial clusters of size 1, one for each item
		List<Cluster> currentClusters = new ArrayList<Cluster>();
		for (SimpleItem item : items) {
			currentClusters.add(item);
		}

		// Keep merging the two closest clusters, until there is only one
		// cluster left
		while (currentClusters.size() > breakK) {
			ClusterPair closestClusters = findClosestClusters(currentClusters);

			Cluster newCluster = new SuperCluster(closestClusters.first,
					closestClusters.second);
			currentClusters.add(newCluster);

			currentClusters.remove(closestClusters.first);
			currentClusters.remove(closestClusters.second);
		}

		return currentClusters;
	}

	private ClusterPair findClosestClusters(List<Cluster> currentClusters) {
		Cluster firstCandidate = null;
		Cluster secondCandidate = null;
		double distanceBetweenThem = Double.MAX_VALUE;

		for (int i = 0; i < currentClusters.size(); i++) {
			Cluster clusterI = currentClusters.get(i);
			for (int j = i + 1; j < currentClusters.size(); j++) {
				Cluster clusterJ = currentClusters.get(j);
				double distance = getDistanceBetween(clusterI, clusterJ);
				if (distance <= distanceBetweenThem) {
					firstCandidate = clusterI;
					secondCandidate = clusterJ;
					distanceBetweenThem = distance;
				}
			}
		}
		return new ClusterPair(firstCandidate, secondCandidate);
	}

	private double getDistanceBetween(Cluster i, Cluster j) {
		switch (type) {
		case AVG_LINKAGE:
			return i.averageLinkageDistance(j);
		case COMPLETE_LINKAGE:
			return i.completeLinkageDistance(j);
		case SINGLE_LINKAGE:
			return i.singleLinkageDistance(j);
		}
		return Double.MAX_VALUE;
	}

	public static enum ClusteringType {
		SINGLE_LINKAGE, COMPLETE_LINKAGE, AVG_LINKAGE;
	}

	int k;
	List<float[]> data;
	int projdim;

	List<float[]> means;

	private void run() {
		List<SimpleItem> l = new ArrayList<SimpleItem>();
		if(weights!=null){
			for(int i = 0 ;i<this.data.size();i++)
				l.add(new SimpleItem(this.data.get(i),this.weights.get(i)));
		}else{
			System.out.println("weights are null");
			for(int i = 0 ;i<this.data.size();i++)
				l.add(new SimpleItem(this.data.get(i),1f));
		}
		
		List<Cluster> superCluster = induceClusters(l, this.k);

		this.means = new ArrayList<float[]>();
		for (Cluster c : superCluster)
			means.add(computeCentroid(c.getValue()));
	}

	private float[] computeCentroid(List<SimpleItem> cluster) {
		if (cluster.size() < 1)
			return null;
		float[] ret = new float[cluster.get(0).value.length];
		
		float divisor = 0;
		for (SimpleItem member : cluster) {
			divisor+=member.weight;
		}
		divisor = 1.0f/divisor;
		
		for (SimpleItem member : cluster) {
			for (int i = 0; i < member.value.length; i++) {
				ret[i] = ret[i] + member.weight*member.value[i] * divisor;
			}
		}

		return ret;
	}

	@Override
	public List<Centroid> getCentroids() {
		if(means == null)run();
		List<Centroid> cents = new ArrayList<>(means.size());
		for(float[] v : this.means)cents.add(new Centroid(v,0));
		return cents;
	}
	
	@Override
	public void reset(int randomseed) {
		means = null;
	}

	@Override
	public RPHashObject getParam() {
		return new SimpleArrayReader(this.data, k);
	}

	public static void main(String[] args) {
		
		GenerateData gen = new GenerateData(4, 100, 2);
		Agglomerative3 kk = new Agglomerative3(ClusteringType.AVG_LINKAGE,gen.data(),4);
		List<Float> weights = new ArrayList<Float>();
		for(float[] f : new GenerateData(1, gen.getData().size(), 1).data){
			weights.add(Math.abs(f[0]));
		}
		kk.setWeights(weights);
//		VectorUtil.prettyPrint(kk.getCentroids());
		System.out.println();
		for(float[] f:gen.data()){
			VectorUtil.prettyPrint(f);
			System.out.println();
		}
		

	}

}
