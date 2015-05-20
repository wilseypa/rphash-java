package edu.uc.rphash.tests.kmeanspp;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;


/**
 * Clustering algorithm based on David Arthur and Sergei Vassilvitski k-means++ algorithm.
 * @see <a href="http://en.wikipedia.org/wiki/K-means%2B%2B">K-means++ (wikipedia)</a>
 * @since 2.0
 * use {@link org.apache.commons.math3.ml.clustering.KMeansPlusPlus} instead
 */

public class KMeansPlusPlus<T extends Clusterable<T>> implements Clusterer {

    /** Strategies to use for replacing an empty cluster. */
    public static enum EmptyClusterStrategy {

        /** Split the cluster with largest distance variance. */
        LARGEST_VARIANCE,

        /** Split the cluster with largest number of points. */
        LARGEST_POINTS_NUMBER,

        /** Create a cluster around the point farthest from its centroid. */
        FARTHEST_POINT,

        /** Generate an error. */
        ERROR

    }

    /** Random generator for choosing initial centers. */
    private final Random random;

    /** Selected strategy for empty clusters. */
    private final EmptyClusterStrategy emptyStrategy;

    private List<float[]> data;
    /** Build a clusterer.
     * <p>
     * The default strategy for handling empty clusters that may appear during
     * algorithm iterations is to split the cluster with largest distance variance.
     * </p>
     * @param random random generator to use for choosing initial centers
     */
    public KMeansPlusPlus(List<float[]> data,int k) {
    	random  =new Random();
    	emptyStrategy= EmptyClusterStrategy.LARGEST_POINTS_NUMBER;
		
		this.k = k;
		maxIterations = 1000;
		points = new ArrayList<>();
		this.data = data;
		for(float[] f: data)
		{
			DoublePoint p = new DoublePoint(f);
			points.add((T)p);
		}

        clusters = null;
    }

    Collection<T> points;
    int maxIterations;
    int k;
    List<Cluster<T>> clusters;
 


    /**
     * Runs the K-means++ clustering algorithm.
     *
     * @param points the points to cluster
     * @param k the number of clusters to split the data into
     * @param maxIterations the maximum number of iterations to run the algorithm
     *     for.  If negative, no maximum will be used
     * @return a list of clusters containing the points
     * @throws Exception 
     */
    public void run() throws Exception {
        // create the initial clusters
        clusters = chooseInitialCenters(points, k, random);
        assignPointsToClusters(clusters, points);

        // iterate through updating the centers until we're done
        final int max = (maxIterations < 0) ? Integer.MAX_VALUE : maxIterations;
        for (int count = 0; count < max; count++) {
            boolean clusteringChanged = false;
            List<Cluster<T>> newClusters = new ArrayList<Cluster<T>>();
            for (final Cluster<T> cluster : clusters) {
                final T newCenter;
                if (cluster.getPoints().isEmpty()) {
                    switch (emptyStrategy) {
                        case LARGEST_VARIANCE :
                            newCenter = getPointFromLargestVarianceCluster(clusters);
                            break;
                        case LARGEST_POINTS_NUMBER :
                            newCenter = getPointFromLargestNumberCluster(clusters);
                            break;
                        case FARTHEST_POINT :
                            newCenter = getFarthestPoint(clusters);
                            break;
                        default :
                        	throw new Exception();
                    }
                    clusteringChanged = true;
                } else {
                    newCenter = cluster.getCenter().centroidOf(cluster.getPoints());
                    if (!newCenter.equals(cluster.getCenter())) {
                        clusteringChanged = true;
                    }
                }
                newClusters.add(new Cluster<T>(newCenter));
            }
            if (!clusteringChanged) {
            	clusters = newClusters;
                //return clusters;
            }
            assignPointsToClusters(newClusters, points);
            clusters = newClusters;
        }
        //return clusters;
    }

    /**
     * Adds the given points to the closest {@link Cluster}.
     *
     * @param <T> type of the points to cluster
     * @param clusters the {@link Cluster}s to add the points to
     * @param points the points to add to the given {@link Cluster}s
     */
    private static <T extends Clusterable<T>> void
        assignPointsToClusters(final Collection<Cluster<T>> clusters, final Collection<T> points) {
        for (final T p : points) {
            Cluster<T> cluster = getNearestCluster(clusters, p);
            cluster.addPoint(p);
        }
    }

    /**
     * Use K-means++ to choose the initial centers.
     *
     * @param <T> type of the points to cluster
     * @param points the points to choose the initial centers from
     * @param k the number of centers to choose
     * @param random random generator to use
     * @return the initial centers
     */
    private static <T extends Clusterable<T>> List<Cluster<T>>
        chooseInitialCenters(final Collection<T> points, final int k, final Random random) {

        final List<T> pointSet = new ArrayList<T>(points);
        final List<Cluster<T>> resultSet = new ArrayList<Cluster<T>>();

        // Choose one center uniformly at random from among the data points.
        final T firstPoint = pointSet.remove(random.nextInt(pointSet.size()));
        resultSet.add(new Cluster<T>(firstPoint));

        final double[] dx2 = new double[pointSet.size()];
        while (resultSet.size() < k) {
            // For each data point x, compute D(x), the distance between x and
            // the nearest center that has already been chosen.
            int sum = 0;
            for (int i = 0; i < pointSet.size(); i++) {
                final T p = pointSet.get(i);
                final Cluster<T> nearest = getNearestCluster(resultSet, p);
                final double d = p.distanceFrom(nearest.getCenter());
                sum += d * d;
                dx2[i] = sum;
            }

            // Add one new data point as a center. Each point x is chosen with
            // probability proportional to D(x)2
            final double r = random.nextDouble() * sum;
            for (int i = 0 ; i < dx2.length; i++) {
                if (dx2[i] >= r) {
                    final T p = pointSet.remove(i);
                    resultSet.add(new Cluster<T>(p));
                    break;
                }
            }
        }

        return resultSet;

    }

    /**
     * Get a random point from the {@link Cluster} with the largest distance variance.
     *
     * @param clusters the {@link Cluster}s to search
     * @return a random point from the selected cluster
     */
    private T getPointFromLargestVarianceCluster(final Collection<Cluster<T>> clusters) {

        double maxVariance = Double.NEGATIVE_INFINITY;
        Cluster<T> selected = null;
        for (final Cluster<T> cluster : clusters) {
            if (!cluster.getPoints().isEmpty()) {

                // compute the distance variance of the current cluster
                final T center = cluster.getCenter();
//                final Variance stat = new Variance();

        		double n = 0;
        		double mean = 0;
        		double M2 = 0;
                
                for (T point : cluster.getPoints()){
                	double x = point.distanceFrom(center);
        			n++;
        			double delta = x - mean;
        			mean = mean + delta/n;
        			M2 = M2 + delta*(x-mean);
                    //stat.increment(point.distanceFrom(center));
                }
                
                final double variance = M2/(n-1f);// =  stat.getResult();

                // select the cluster with the largest variance
                if (variance > maxVariance) {
                    maxVariance = variance;
                    selected = cluster;
                }

            }
        }

        // did we find at least one non-empty cluster ?
        if (selected == null) {
        	System.out.println("Convergence Error");
        	return null;
//            throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
        }

        // extract a random point from the cluster
        final List<T> selectedPoints = selected.getPoints();
        return selectedPoints.remove(random.nextInt(selectedPoints.size()));

    }

    /**
     * Get a random point from the {@link Cluster} with the largest number of points
     *
     * @param clusters the {@link Cluster}s to search
     * @return a random point from the selected cluster
     */
    private T getPointFromLargestNumberCluster(final Collection<Cluster<T>> clusters) {

        int maxNumber = 0;
        Cluster<T> selected = null;
        for (final Cluster<T> cluster : clusters) {

            // get the number of points of the current cluster
            final int number = cluster.getPoints().size();

            // select the cluster with the largest number of points
            if (number > maxNumber) {
                maxNumber = number;
                selected = cluster;
            }

        }

        if (selected == null) {
        	System.out.println("Convergence Error");
        	return null;
//            throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
        }

        // extract a random point from the cluster
        final List<T> selectedPoints = selected.getPoints();
        return selectedPoints.remove(random.nextInt(selectedPoints.size()));

    }

    /**
     * Get the point farthest to its cluster center
     *
     * @param clusters the {@link Cluster}s to search
     * @return point farthest to its cluster center
     */
    private T getFarthestPoint(final Collection<Cluster<T>> clusters) {

        double maxDistance = Double.NEGATIVE_INFINITY;
        Cluster<T> selectedCluster = null;
        int selectedPoint = -1;
        for (final Cluster<T> cluster : clusters) {

            // get the farthest point
            final T center = cluster.getCenter();
            final List<T> points = cluster.getPoints();
            for (int i = 0; i < points.size(); ++i) {
                final double distance = points.get(i).distanceFrom(center);
                if (distance > maxDistance) {
                    maxDistance     = distance;
                    selectedCluster = cluster;
                    selectedPoint   = i;
                }
            }

        }

        // did we find at least one non-empty cluster ?
        if (selectedCluster == null) {
        	System.out.println("Convergence Error");
        	return null;
//            throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
        }

        return selectedCluster.getPoints().remove(selectedPoint);

    }

    /**
     * Returns the nearest {@link Cluster} to the given point
     *
     * @param <T> type of the points to cluster
     * @param clusters the {@link Cluster}s to search
     * @param point the point to find the nearest {@link Cluster} for
     * @return the nearest {@link Cluster} to the given point
     */
    private static <T extends Clusterable<T>> Cluster<T>
        getNearestCluster(final Collection<Cluster<T>> clusters, final T point) {
        double minDistance = Double.MAX_VALUE;
        Cluster<T> minCluster = null;
        for (final Cluster<T> c : clusters) {
            final double distance = point.distanceFrom(c.getCenter());
            if (distance < minDistance) {
                minDistance = distance;
                minCluster = c;
            }
        }
        return minCluster;
    }

	@Override
	public List<float[]> getCentroids() {
		
		ArrayList<float[]> ret = new ArrayList<>();
		if(clusters == null)
			try {
				run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		for(Cluster<T> c : clusters){
			double[] cntr = ((DoublePoint)c.getCenter()).getPoint();
			float[] retret = new float[cntr.length];
			
			for(int i = 0;i<cntr.length;i++ ){
				retret[i] = (float) cntr[i];
			}
			ret.add(retret);
		}
		
		return ret;
	}

	@Override
	public RPHashObject getParam() {
		
		return new SimpleArrayReader(data, k);
	}
	public static void main(String[] args){
		GenerateData gen = new GenerateData(3,3000,2,.1f);
		KMeansPlusPlus<DoublePoint> kk = new KMeansPlusPlus<>(gen.data(),3);

		
		List<float[]> aligned = TestUtil.alignCentroids(
				kk.getCentroids(), gen.medoids());
		
		System.out.println( StatTests.PR(aligned, gen) + ":"+StatTests.SSE(aligned, gen));
		System.gc();

	}
	

}
