package edu.uc.rphash.decoders;

import java.util.Random;

import edu.uc.rphash.tests.TestUtil;


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

	    public float[] closestPoint(float[] y) {
	        if (n != y.length) throw new RuntimeException("y is the wrong length");
	        
	        round(y, u);
	        int m = (int)Math.rint(sum(u));
	        if( mod(m, 2) == 1){
	            int k = 0;
	            float D = Float.NEGATIVE_INFINITY;
	            for(int i = 0; i < n; i++){
	                float d = Math.abs(y[i] - u[i]);
	                if( d > D){
	                    k = i;
	                    D = d;
	                }
	            }
	            u[k] += Math.signum(y[k] - u[k]);
	        }
	        
	        dist = dist(u, y);
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
	public byte[] decode(float[] f) {
		float[] tmp = closestPoint(f);
		byte[] tmpbyte = new byte[n];
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
		int K = 6;
		int L = 2;
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

				distavg += TestUtil.distance(p1, p2);
				byte[] hp1 = sp.decode(p1);
				byte[] hp2 = sp.decode(p2);
				boolean test = true;
				for (int k = 0; k < hp1.length && test == true; k++) {
					if (hp1[k] != hp2[k])
						test = false;
				}
				if (test)
					ct++;
			}
			System.out.println(distavg / 10000f + "\t" + (float) ct / 10000f);
		}
	}
	

}
