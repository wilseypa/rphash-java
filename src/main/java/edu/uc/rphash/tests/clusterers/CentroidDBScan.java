package edu.uc.rphash.tests.clusterers;
import java.util.List; 
 
import org.apache.commons.math3.ml.clustering.CentroidCluster; 
import org.apache.commons.math3.ml.clustering.Cluster; 
import org.apache.commons.math3.ml.clustering.Clusterable; 
import org.apache.commons.math3.ml.clustering.DoublePoint; 
import org.apache.commons.math3.ml.distance.DistanceMeasure; 
import org.apache.commons.math3.ml.distance.EuclideanDistance; 
 
/**
 * Base class for cluster evaluation methods. 
 * 
 * @param <T> type of the clustered points 
 * @since 3.3 
 */ 
public class CentroidDBScan<T extends Clusterable> { 
 
    /** The distance measure to use when evaluating the cluster. */ 
    private final DistanceMeasure measure; 
 
    /**
     * Creates a new cluster evaluator with an {@link EuclideanDistance} 
     * as distance measure. 
     */ 
    public CentroidDBScan() { 
        this(new EuclideanDistance()); 
    } 
 
    /**
     * Creates a new cluster evaluator with the given distance measure. 
     * @param measure the distance measure to use 
     */ 
    public CentroidDBScan(final DistanceMeasure measure) { 
        this.measure = measure; 
    } 
 
    /**
     * Computes the centroid for a cluster. 
     * 
     * @param cluster the cluster 
     * @return the computed centroid for the cluster, 
     * or {@code null} if the cluster does not contain any points 
     */ 
    public Clusterable centroidOf(final Cluster<T> cluster) { 
        final List<T> points = cluster.getPoints(); 
        if (points.isEmpty()) { 
            return null; 
        } 
 
        // in case the cluster is of type CentroidCluster, no need to compute the centroid 
        if (cluster instanceof CentroidCluster) { 
            return ((CentroidCluster<T>) cluster).getCenter(); 
        } 
 
        final int dimension = points.get(0).getPoint().length; 
        final double[] centroid = new double[dimension]; 
        for (final T p : points) { 
            final double[] point = p.getPoint(); 
            for (int i = 0; i < centroid.length; i++) { 
                centroid[i] += point[i]; 
            } 
        } 
        for (int i = 0; i < centroid.length; i++) { 
            centroid[i] /= points.size(); 
        } 
        return new DoublePoint(centroid); 
    } 
 
}
