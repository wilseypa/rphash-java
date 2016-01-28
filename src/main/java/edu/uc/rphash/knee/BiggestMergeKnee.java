package edu.uc.rphash.knee;

import edu.uc.rphash.util.VectorUtil;

public class BiggestMergeKnee implements KneeAlgorithm {

	@Override
	public int findKnee(float[] data) {

		return data.length/2;
	}
	

	

	/**
	 * this function creates a linear model y=alpha*x+beta for the given data
	 * series x,y.
	 */
	float[] linest(float[] y) {

		int n = y.length;
		float[] x = new float[n];
		for(int i = 0;i<n;i++){
			x[i] = i;
		}
		float sx = VectorUtil.sum(x);
		float sy = VectorUtil.sum(y);
		float sxx = VectorUtil.dotSum(x, x);
		float sxy = VectorUtil.dotSum(x, y);
		float syy = VectorUtil.dotSum(y, y);
		float beta = (((float)n) * sxy - sx * sy) / (((float)n) * sxx - sx * sx);
		float alpha = sy / ((float)n) - beta * sx / ((float)n);
		float r = (float) Math.sqrt((n*sxy-sx*sy)/((n*sxx-sx*sx)*(n*syy-sy*sy)));
		float rsquared = r*r;
		return new float[]{beta,alpha,rsquared};
	}
}
