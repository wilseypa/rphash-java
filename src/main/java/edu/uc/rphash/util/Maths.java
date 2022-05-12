package edu.uc.rphash.util;

import java.util.*;


//import org.apache.commons.math.*;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

// taken from " https://github.com/lukehb/137-common/blob/master/src/main/java/onethreeseven/common/util/Maths.java   by  Luke Bermingham  "
/**
 * A utility of mathematical methods.
 */



public final class Maths {

    /**
     * The height of normal distribution gaussian with std 1 and mean = 0.
     */
    public static final double GaussHeight = 1.0/Math.sqrt(2 * Math.PI);

    private Maths() {
    }

    /**
     * Find the angle (in degrees) between two points, p1 and p2.
     * @param x1 P1.x
     * @param y1 P1.y
     * @param x2 P2.x
     * @param y2 P2.y
     * @return The angle between p1 and p2 (in degrees).
     */
    public static double angleBetween(double x1, double y1, double x2, double y2){
        double angle = Math.atan2(y2, x2) - Math.atan2(y1, x1);
        if (angle < 0){
            angle += 2 * Math.PI;
        }
        return Math.toDegrees(angle);
    }

    /**
     * Find the point with the minimal pairwise distance between all points.
     * @param pts some points
     * @return The medoid point from the points data-set (i.e. the most central point).
     */
    public static double[] medoid(double[][] pts){
        double bestDist = Double.MAX_VALUE;
        double[] medoid = null;

        for (int i = 0; i < pts.length; i++) {
            double[] pt = pts[i];
            double totalDisp = 0;
            for (int j = 0; j < pts.length; j++) {
                if(j == i){continue;}
                totalDisp += dist(pts[i], pts[j]);
            }
            if(totalDisp < bestDist){
                bestDist = totalDisp;
                medoid = pt;
            }
        }
        return medoid;
    }

    /**
     * Find the maximum gap in a series of numerical data and find the middle of that largest gap.
     * @param data The 1d numerical data.
     * @return The largest gap.
     */
    public static double[] maxGap(double[] data){
        double[] sorted = new double[data.length];
        System.arraycopy(data, 0, sorted, 0, data.length);
        Arrays.sort(sorted);

        double maxGap = 0;
        double[] minMax = new double[]{0,0};

        for (int i = 1; i < data.length; i++) {
            double gap = sorted[i] - sorted[i-1];
            if(gap > maxGap){
                maxGap = gap;
                minMax[0] = sorted[i-1];
                minMax[1] = sorted[i];
            }
        }
        return minMax;
    }

    /**
     * The euclidean distance between two n-d points (order doesn't matter).
     * @param a Point a
     * @param b Point b
     * @return The euclidean distance between two points.
     */
    public static double dist(double[] a, double[] b){
        return Math.sqrt(Maths.distSq(a,b));
    }

    /**
     * Returns the euclidean distance squared between two n-d points.
     * @param a Point a.
     * @param b Point b.
     * @return The euclidean distance squared between two points.
     */
    public static double distSq(double[] a, double[] b){
        double distSq = 0;
        for (int i = 0; i < a.length; i++) {
            distSq += Math.pow(a[i] - b[i], 2);
        }
        return distSq;
    }

    /**
     * @param x The variable input into the function.
     * @param height The height of the center of the curve (sometimes called 'a').
     * @param center The center of the curve (sometimes called 'b').
     * @param width The standard deviation, i.e ~68% of the data will be contained in center ± the width.
     * @return A gaussian function.
     */
    public static double gaussian(double x, double height, double center, double width){
        return height * Math.exp(-(x-center)*(x-center)/(2.0*width*width) );
    }

    public static long mean(long[] d){
        long total = 0;
        for (long v : d) {
            total += v;
        }
        return total/d.length;
    }

    public static void shuffle(int[] array){
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--)
        {
            int index = rand.nextInt(i + 1);
            // Simple swap
            int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }

    public static double mean(double[] d){
        double total = 0;
        for (double v : d) {
            total += v;
        }
        return total/d.length;
    }

    public static double std(double[] data){
        double mean = mean(data);
        double std = 0;
        for (double d : data) {
            double deviation = d - mean;
            std += deviation * deviation;
        }
        std /= data.length;
        return Math.sqrt(std);
    }

    public static long[] absDeviationsFromMedian(long[] data){
        long median = median(data);
        long[] deviations = new long[data.length];
        for (int i = 0; i < data.length; i++) {
            deviations[i] = Math.abs(data[i] - median);
        }
        return deviations;
    }

    /**
     * Calculate the absolute deviations a sample has away from its median.
     * @param data The data to determine median and deviations for.
     * @return An array of absolute deviations away from the median.
     */
    public static double[] absDeviationsFromMedian(double[] data){
        double median = median(data);
        double[] deviations = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            deviations[i] = Math.abs(data[i] - median);
        }
        return deviations;
    }

    /**
     * Linearly interpolate resolve a starting point towards some ending point.
     * @param startPt The point to start at.
     * @param endPt The point to head towards.
     * @param alpha A value of 0 ends at the start pt, a value of 1 ends at the end point, a value
     *              greater than 1 over shoots the end point but continues following that same
     *              direction, likewise, a negative value heads backwards resolve the starting point
     *              with the end point reachable in a straight line.
     * @return The newly interpolated position.
     */
    public static double[] lerp(double[] startPt, double[] endPt, double alpha){
        if(startPt.length != endPt.length){
            throw new IllegalArgumentException("Start and end must have equal lengths.");
        }
        //we use c as the direction, and then as the final output
        double[] c = new double[startPt.length];
        for (int i = 0; i < startPt.length; i++) {
            c[i] = startPt[i] + ( (endPt[i] - startPt[i]) * alpha );
        }
        return c;
    }

    /**
     * Do an element-wise subtraction such that, result[i] = a[i] - b[i].
     * @param a array a
     * @param b array b
     * @return the resulting "subtracted" result[] array.
     */
    public static double[] sub(double[] a, double[] b){
        if(a.length != b.length){
            throw new IllegalArgumentException("Array A and B must be the same length.");
        }
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    /**
     * Do an element-wise subtraction such that, result[i] = a[i] - b[i].
     * @param a array a
     * @param b array b
     * @return the resulting "subtracted" result[] array.
     */
    public static int[] sub(int[] a, int[] b){
        if(a.length != b.length){
            throw new IllegalArgumentException("Array A and B must be the same length.");
        }
        int[] result = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    /**
     * @param a the array
     * @return The maximum absolute element in the array 'a'.
     */
    public static int maxAbsElement(double[] a){
        int max = (int) Math.abs(a[0]);
        for (int i = 1; i < a.length; i++) {
            int element = (int) Math.abs(a[i]);
            if(element > max){
                max = element;
            }
        }
        return max;
    }

    /**
     * Divide every element in array 'a' by a given scalar.
     * @param a the array
     * @param scalar the divisor
     * @return The array such that, result[i] = a[i] / scalar
     */
    public static double[] div(double[] a, double scalar){
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] / scalar;
        }
        return result;
    }

    /**
     * Find the area of a triangle defined by three points: a,b,c.
     * @param ax X coordinate of point a.
     * @param ay Y coordinate of point a.
     * @param bx X coordinate of point b.
     * @param by Y coordinate of point b.
     * @param cx X coordinate of point c.
     * @param cy Y coordinate of point c.
     * @return The area of the triangle.
     */
    public static double triArea(double ax, double ay,
                                 double bx, double by,
                                 double cx, double cy){
        return Math.abs((ax - cx) * (by - ay) - (ax - bx) * (cy - ay)) * 0.5;
    }

    public static double triArea3D(double ax, double ay, double az,
                                   double bx, double by, double bz,
                                   double cx, double cy, double cz) {
        return 0.5 * Math.sqrt(dotSq(ax, ay, bx, by, cx, cy) +
                dotSq(ax, az, bx, bz, cx, cz) + dotSq(ay, az, by, bz, cy, cz));
    }

    /**
     * Returns the cross product of two 3d vectors.
     * @param a 3d vector "a".
     * @param b 3d vector "b".
     * @return The cross product of a and b. In other words, the vector that is orthogonal to a and b.
     */
    public static double[] cross3d(double[] a, double[] b){
        if(a.length != 3){
            throw new IllegalArgumentException("Vector a length must equal 3.");
        }
        if(b.length != 3){
            throw new IllegalArgumentException("Vector b length must equal 3.");
        }
        return new double[]{
                a[1]*b[2] - a[2]*b[1],
                a[2]*b[0] - a[0]*b[2],
                a[0]*b[1] - a[1]*b[0]
        };
    }

    /**
     * Dot vector "a" against vector "b".
     * That is, if a = [a1,a2,...,an] and b = [b1,b2,...,bn]
     * then a dot b = a1*b1 + a2*b2 + ... + an*bn.
     * @param a Vector a.
     * @param b Vector b.
     * @return The result of a dot b.
     */
    public static double dot(double[] a, double[] b){
        if(a.length != b.length){
            throw new IllegalArgumentException("Vector 'a' must have the same length as vector 'b'.");
        }
        double dotProduct = 0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i]*b[i];
        }
        return dotProduct;
    }

    /**
     * Multiply every component in "a" by a scalar.
     * @param a The vector "a".
     * @param scalar The scalar.
     * @return A new vector. Original "a" is not modified.
     */
    public static double[] scale(double[] a, double scalar){
        double[] aPrime = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            aPrime[i] = a[i] * scalar;
        }
        return aPrime;
    }

    /**
     * Add vectors a and b together in a component-wise fashion.
     * @param a Vector a.
     * @param b Vector b.
     * @return Return vector a plus vector b in a new vector.
     */
    public static double[] add(double[] a, double[] b){
        if(a.length != b.length){
            throw new IllegalArgumentException("Vector a and b must have the same lengths.");
        }
        double[] c = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] + b[i];
        }
        return c;
    }

    /**
     * Get the perpendicular distance between a point and a line formed by two other points.
     * @param start The start point of the line.
     * @param end The end point of the line.
     * @param otherPt The other point to get the perpendicular distance from.
     * @return The perpendicular distance between the point and the line.
     */
    public static double perpendicularDistance(double[] start, double[] end, double[] otherPt){
        if(start.length != end.length || start.length != otherPt.length){
            throw new IllegalArgumentException("Vectors must have equal lengths.");
        }
        double[] projectedPt = projectAlong(start, end, otherPt);
        return Maths.dist(otherPt, projectedPt);
    }

    /**
     * Given a line formed by start and end points and some other point,
     * project that other point onto the line.
     * @param start The start point.
     * @param end The end point.
     * @param otherPt The other point.
     * @return The point projected onto the line.
     */
    public static double[] projectAlong(double[] start, double[] end, double[] otherPt){
        if(start.length != end.length || start.length != otherPt.length){
            throw new IllegalArgumentException("Vectors must have equal lengths.");
        }
        double[] ab = Maths.sub(otherPt,start);
        double[] ac = Maths.sub(end,start);
        double percentageAlong = Maths.dot(ab, ac) / Maths.dot(ac, ac);
        double[] amountMovedAC = Maths.scale(ac, percentageAlong);
        return Maths.add(start, amountMovedAC);
    }

    private static double dotSq(double ax, double ay, double bx, double by, double cx, double cy) {
        double dot = ax * by - ax * cy + bx * cy - bx * ay + cx * ay - cx * by;
        return dot * dot;
    }

    public static double median(double[] data){
        if(data.length == 0){
            return Double.NaN;
        }
        double[] d = new double[data.length];
        System.arraycopy(data, 0, d, 0, data.length);
        Arrays.sort(d);
        int len = d.length;
        if(len == 1){
            return d[0];
        }
        //even case
        else if(len % 2 == 0){
            int midRightIdx = (d.length) / 2;
            int midLeftIdx = midRightIdx - 1;
            return  (d[midRightIdx] + d[midLeftIdx]) / 2.0;
        }
        //odd case
        else{
            int midIdx = (d.length - 1) / 2;
            return d[midIdx];
        }
    }

    public static long median(long[] data){
        if(data.length == 0){
            throw new IllegalArgumentException("Data must have at least one element to find median.");
        }
        long[] d = new long[data.length];
        System.arraycopy(data, 0, d, 0, data.length);
        Arrays.sort(d);
        int len = d.length;
        if(len == 1){
            return d[0];
        }
        //even case
        else if(len % 2 == 0){
            int midRightIdx = (d.length) / 2;
            int midLeftIdx = midRightIdx - 1;
            return (long) ((d[midRightIdx] + d[midLeftIdx]) / 2.0);
        }
        //odd case
        else{
            int midIdx = (d.length - 1) / 2;
            return d[midIdx];
        }

    }

    public static double mode(double[] data){
        HashMap<Double, Integer> tally = new HashMap<>();

        for (double v : data) {
            int nOccurrences = tally.getOrDefault(v, 0) + 1;
            tally.put(v, nOccurrences);
        }

        Optional<Map.Entry<Double, Integer>> modalOpt =
                tally.entrySet().stream().max((o1, o2) -> Integer.compare(o1.getValue(), o2.getValue()));

        if(modalOpt.isPresent()){
            return modalOpt.get().getKey();
        }

        return Double.NaN;
    }

    /**
     * Smooth the data using a gaussian kernel.
     * @param data The data to smooth.
     * @param n The size of sliding window (i.e number of indices either side to sample).
     * @return The smoothed version of the data.
     */
    public static double[] gaussianSmooth(double[] data, int n){
        double[] smoothed = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            int startIdx = Math.max(0, i - n);
            int endIdx = Math.min(data.length - 1, i + n);

            double sumWeights = 0;
            double sumIndexWeight = 0;

            for (int j = startIdx; j < endIdx + 1; j++) {
                double indexScore = Math.abs(j - i)/(double)n;
                double indexWeight = Maths.gaussian(indexScore, 1, 0, 1);
                sumWeights += (indexWeight * data[j]);
                sumIndexWeight += indexWeight;
            }
            smoothed[i] = sumWeights/sumIndexWeight;
        }
        return smoothed;
    }

    /**
     * Smooth the data using a gaussian kernel.
     * @param data The data to smooth.
     * @param w The size of sliding window (i.e number of indices either side to sample).
     * @return The smoothed version of the data.
     */
    public static double[][] gaussianSmooth2d(double[][] data, int w){
        final int dataSize = data.length;

        if(dataSize == 0){
            throw new IllegalArgumentException("Cannot smooth empty data.");
        }

        final int nDims = data[0].length;

        if(nDims == 0){
            throw new IllegalArgumentException("Cannot smooth a data point with no values. " +
                    "Uniformly populate every entry in your data with 1 or more dimensions.");
        }

        double[][] smoothed = new double[dataSize][nDims];

        for (int i = 0; i < dataSize; i++) {
            int startIdx = Math.max(0, i - w);
            int endIdx = Math.min(dataSize - 1, i + w);

            double[] sumWeights = new double[nDims];
            double sumIndexWeight = 0;

            for (int j = startIdx; j < endIdx + 1; j++) {
                double indexScore = Math.abs(j - i)/(double)w;
                double indexWeight = Maths.gaussian(indexScore, 1, 0, 1);

                for (int n = 0; n < nDims; n++) {
                    sumWeights[n] += (indexWeight * data[j][n]);
                }
                sumIndexWeight += indexWeight;
            }

            for (int n = 0; n < nDims; n++) {
                smoothed[i][n] = sumWeights[n]/sumIndexWeight;
            }
        }
        return smoothed;
    }

    public static double[][] Smooth2d(double[][] data){
    	// double linearInterp(double[] x, double[] y, double xi)
    	
    		int size  = data.length;                                //50
    		double x[] = new double[size];
    		double xi[] = new double[size];
    		double y[] = new double[size];
    		double smooth_xy[][] =new double[size][2];
    		
    		for ( int i=0 ;   i<=size-1 ;  i++) {
    			 x[i] = data[(size-1)-i][1];
    			 y[i] = data[i][0]; 
    			
    		}
    	
    	 // return linear interpolation of (x,y) on xi
    	   LinearInterpolator li = new LinearInterpolator();
    	 // 
    	   
    	   PolynomialSplineFunction psf = li.interpolate(x,y);
    	   
    	   for ( int i=0 ;   i<=size-1 ;  i++) {
    		   
   			smooth_xy[(size-1)-i][1]= x[i];
   			
   			smooth_xy[i][0]= psf.value(x[i]); 
   			
   			}	   		
    	       	   
    	   
    	   return smooth_xy;
    		
    	
    }
    	
    
    
    /**
     * Normalise the 1d data using min-max normalisation.
     * @see <a href="https://en.wikipedia.org/wiki/Feature_scaling#Rescaling">Wikipedia article about feature re-scaling.</a>
     * @param data The data to normalise.
     * @return The new array containing the normalised data.
     */
    public static double[] minmaxNormalise1d(double[] data){
        //find min and max value
        double curMin = Double.POSITIVE_INFINITY;
        double curMax = Double.NEGATIVE_INFINITY;
        for (double v : data) {
            if(v < curMin){
                curMin = v;
            }
            if(v > curMax){
                curMax = v;
            }
        }

        //normalise the data using min-max normalisation
        //and also subtract each value from its normalised index
        final double range = curMax - curMin;
        double[] normalisedData = new double[data.length];

        for (int i = 0; i < normalisedData.length; i++) {
            normalisedData[i] = ((data[i] - curMin) / range);
        }
        return normalisedData;
    }

    /**
     * Performs min-max normalisation on n-dimensional data (as long as the dimensionality is uniform, that is, all data is 2d or all 3d etc.).
     * @see <a href="https://en.wikipedia.org/wiki/Feature_scaling#Rescaling">Wikipedia article about feature re-scaling.</a>
     * @param data The data to normalised.
     * @return A new normalised data-set.
     */
    public static double[][] minmaxNormalise(double[][] data){

        final int dataSize = data.length;

        if(dataSize == 0){
            throw new IllegalArgumentException("Cannot smooth empty data.");
        }

        final int nDims = data[0].length;

        if(nDims == 0){
            throw new IllegalArgumentException("Cannot smooth a data point with no values. " +
                    "Uniformly populate every entry in your data with 1 or more dimensions.");
        }

        //1) get min and max for each dimension of the data

        double[] minEachDim = new double[nDims];
        double[] maxEachDim = new double[nDims];
        for (int i = 0; i < nDims; i++) {
            minEachDim[i] = Double.POSITIVE_INFINITY;
            maxEachDim[i] = Double.NEGATIVE_INFINITY;
        }

        for (double[] coords : data) {
            for (int n = 0; n < nDims; n++) {
                double v = coords[n];
                if (v < minEachDim[n]) {
                    minEachDim[n] = v;
                }
                if (v > maxEachDim[n]) {
                    maxEachDim[n] = v;
                }
            }
        }

        //2) normalise the data using the min and max
        double[] rangeEachDim = new double[nDims];
        for (int n = 0; n < nDims; n++) {
            rangeEachDim[n] = maxEachDim[n] - minEachDim[n];
        }

        double[][] outputNormalised = new double[dataSize][nDims];
        for (int i = 0; i < dataSize; i++) {
            for (int n = 0; n < nDims; n++) {
                //normalising step
                outputNormalised[i][n] = (data[i][n] - minEachDim[n]) / rangeEachDim[n];
            }
        }
        return outputNormalised;
    }

}