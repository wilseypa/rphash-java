package edu.uc.rphash.tests.kmeanspp;

import java.util.Collection;

public interface Clusterable<T> {

    /**
     * Returns the distance from the given point.
     *
     * @param p the point to compute the distance from
     * @return the distance from the given point
     */
    double distanceFrom(T p);

    /**
     * Returns the centroid of the given Collection of points.
     *
     * @param p the Collection of points to compute the centroid of
     * @return the centroid of the given Collection of Points
     */
    T centroidOf(Collection<T> p);

}