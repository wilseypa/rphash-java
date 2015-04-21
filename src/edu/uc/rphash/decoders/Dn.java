package edu.uc.rphash.decoders;


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
		// TODO Auto-generated method stub
		return 0;
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

	

}
