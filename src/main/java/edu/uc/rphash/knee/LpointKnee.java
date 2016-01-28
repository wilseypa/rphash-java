package edu.uc.rphash.knee;

import edu.uc.rphash.util.VectorUtil;

/** This knee point finding algorithm is similar to the L-Point Method
 * however it does not yet recursively remove overfitting for long
 * tail regions as in the latter section of the L-Point Method description
 * @author lee
 *
 */
public class LpointKnee implements KneeAlgorithm {

	@Override
	public int findKnee(float[] data) {

		return findFurthest(data);
	}
	
	/** this heurisitic selects the point that is furthest from a linear fit
	 * of the function as the knee point.
	 * @param y
	 * @return
	 */
	int findFurthest(float[] y) {
		float[] params =  linest(y);
		float beta = params[0];
		float alpha = params[1];
		
		float maxdist = 0;
		int argmax = 0;
		for(int i = 0;i<y.length;i++){
			float tmpdist = Math.abs(y[i]-((float)i)*beta+alpha);
			if(tmpdist>maxdist){
				maxdist = tmpdist;
				argmax = i;
			}
		}
		return argmax;
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
