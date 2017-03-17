package edu.uc.rphash.decoders;

import java.util.Random;

import edu.uc.rphash.frequentItemSet.Countable;
import edu.uc.rphash.util.VectorUtil;


public class Dn implements Decoder {

	float[] u, v;
	int n;
	float dist;

	public Dn(int n) {
		this.n = n;
		u = new float[n];
		v = new float[n];
	}
	
	static float dist(float[] x, float[] s) {
	    /**
	     * Euclidean distance between two vectors
	     */
	        float out = 0.0f;
	        for (int i = 0; i < x.length; i++) {
	            out += Math.pow(x[i] - s[i], 2.0);
	        }
	        return (float)Math.sqrt(out);
	    
	}


    public static float sum(float[] x) {
        float out = 0.0f;
        for (int i = 0; i < x.length; i++) {
            out += x[i];
        }
        return out;
    }

	   public static void round(float[] x, float[] y) {
	        for (int i = 0; i < x.length; i++) {
	            y[i] = Math.round(x[i]);
	        }
	    }

	public static int mod(int x, int y) {
		int t = x % y;
		if (t < 0) {
			t += y;
		}
		return t;
	}

//	 public float[] scale(float[] y) 
//	 {
//		 float[] ycopy = new float[y.length];
//		 for(int i = 0;i<ycopy.length;i++) 
//			 ycopy[i] = y[i];
//		 return ycopy;
//	 }
	
	    /** This function returns an e8 encoded vector scaled between -1:3
	     * any comparisons will have to be rescaled
	     * @param y
	     * @return e8 codeword
	     */
	    public float[] closestPoint(float[] y) {
	    	float[] ycopy =  new float[y.length];
	    	System.arraycopy(y, 0, ycopy, 0, y.length);
	        if (n != ycopy.length) throw new RuntimeException("y is the wrong length");
	        
	        round(ycopy, u);
	        int m = (int)Math.rint(sum(u));
	        if( mod(m, 2) == 1){
	            int k = 0;
	            float D = Float.NEGATIVE_INFINITY;
	            for(int i = 0; i < n; i++){
	                float d = Math.abs(ycopy[i] - u[i]);
	                if( d > D){
	                    k = i;
	                    D = d;
	                }
	            }
	            u[k] += Math.signum(ycopy[k] - u[k]);
	        }
	        dist = dist(u, ycopy);
	        return u;
	    }
	    

	    public float distance(){
	        return dist;
	    }


	    public float[] getLatticePoint() {
	        return u;
	    }

	@Override
	public int getDimensionality() {
		return n;
	}

	@Override
	public long[] decode(float[] f) {
		float[] tmp = closestPoint(f);
		long[] tmpbyte = new long[n];
		for(int i = 0;i<n;i++)tmpbyte[i] = (byte)tmp[i];
		return tmpbyte;

	}

	@Override
	public float getErrorRadius() {
		return 0;
	}

	@Override
	public float getDistance() {
		return dist;
	}

	public static void main(String[] args) {
		Random r = new Random();
		int d = 64;
//		int K = 6;
//		int L = 2;
		Dn sp = new Dn(64);
		for (int i = 0; i < 100; i++) {
			int ct = 0;
			float distavg = 0.0f;
			for (int j = 0; j < 10000; j++) {
				float p1[] = new float[d];
				float p2[] = new float[d];
				for (int k = 0; k < d; k++) {
					p1[k] = r.nextFloat() * 2 - 1;
					p2[k] = (float) (p1[k] + r.nextGaussian()
							* ((float) i / 1000f));
				}

				distavg+=VectorUtil.distance(p1,p2);
				long[] hp1 = sp.decode(VectorUtil.normalize(p1));
				long[] hp2 = sp.decode(VectorUtil.normalize(p2));
				boolean test = false;
				for(int k = 0; k< hp1.length;k++)
				{
					if(hp1[k]==hp2[k]){
						test=true;
						
					}
				}
				if(test)ct++;
			}
			System.out.println(distavg / 10000f + "\t" + (float) ct / 10000f);
		}
	}

//	float [] variance;
//	float varTot = 1;
//	@Override
//	public void setVariance(float[] parameterObject) {
//		varTot = 0;
//		for(int i = 0 ; i<this.getDimensionality();i++)varTot+=this.variance[i];
//		varTot/=(float)this.getDimensionality();
//		variance = parameterObject;
//	}

	@Override
	public boolean selfScaling() {
		return false;
	}

	@Override
	public void setCounter(Countable counter) {
		// TODO Auto-generated method stub
		
	}
	
//	@Override
//	public float[] getVariance(){
//		return variance;
//	}
	
	

}
