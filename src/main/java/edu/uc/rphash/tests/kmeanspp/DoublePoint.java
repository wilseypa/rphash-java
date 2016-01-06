package edu.uc.rphash.tests.kmeanspp;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import edu.uc.rphash.util.VectorUtil;

public class DoublePoint implements Clusterable<DoublePoint>, Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 3946024775784901369L;

    /** Point coordinates. */
    private final double[] point;

    /**
     * Build an instance wrapping an double array.
     * <p>
     * The wrapped array is referenced, it is <em>not</em> copied.
     *
     * @param point the n-dimensional point in double space
     */
    public DoublePoint(final double[] point) {
        this.point = point;
    }

    /**
     * Build an instance wrapping an integer array.
     * <p>
     * The wrapped array is copied to an internal double array.
     *
     * @param point the n-dimensional point in integer space
     */
    public DoublePoint(final int[] point) {
        this.point = new double[point.length];
        for ( int i = 0; i < point.length; i++) {
            this.point[i] = point[i];
        }
    }

    public DoublePoint(float[] point) {
        this.point = new double[point.length];
        for ( int i = 0; i < point.length; i++) {
            this.point[i] = point[i];
        }
	}

	/** {@inheritDoc} */
    public double[] getPoint() {
        return point;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof DoublePoint)) {
            return false;
        }
        return Arrays.equals(point, ((DoublePoint) other).point);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Arrays.hashCode(point);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Arrays.toString(point);
    }

    public double distanceFrom(final DoublePoint p) {
	        return VectorUtil.distance(point, p.getPoint());
	    }
	    /** {@inheritDoc} */
	  @Override
	    public DoublePoint centroidOf(final Collection<DoublePoint> points) {
	        int[] centroid = new int[getPoint().length];
	        for (DoublePoint p : points) {
	            for (int i = 0; i < centroid.length; i++) {
	                centroid[i] += p.getPoint()[i];
	            }
	        }
	        for (int i = 0; i < centroid.length; i++) {
	            centroid[i] /= points.size();
	        }
	        return new DoublePoint(centroid);
	    }

	
//	public Object centroidOf(Collection p) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}