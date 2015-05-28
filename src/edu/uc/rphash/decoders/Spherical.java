package edu.uc.rphash.decoders;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.tests.TestUtil;

public class Spherical implements Decoder {
	int HashBits = 64;
	List<List<float[]>> vAll; // vAll[i][j] is the vector $A_i \tilde v_j$ from
				// the article.
	int hbits; // Ceil(Log2(2*d)).
	int d; // the dimension of the feature space.
	int k; // number of elementary hash functions (h) to be concataneted to
			// obtain a reliable enough hash function (g). LSH queries becomes
			// more selective with increasing k, due to the reduced the
			// probability of collision.
	int l; // number of "copies" of the bins (with a different random matrices).
			// Increasing L will increase the number of points the should be
			// scanned linearly during query.
	float distance = 0;

	public Spherical(int d, int k, int L) {
		this.d = d;
		this.k = k;
		this.l = L;
		double nvertex = 2.0 * this.d;
		this.hbits = (int) Math.ceil(Math.log(nvertex) / Math.log(2));
		int kmax = (int) (HashBits / this.hbits);
		if (this.k > kmax) {
			this.k = kmax;
			System.out
					.printf("k is too big, chopping down (%d->%d)\n", k, kmax);
		}

		Random[] r = new Random[d];
		for(int i = 0 ; i< d;i++)r[i] = new Random();

		// For orthoplex, the basis Vectortors v_i are permutations of the
		// Vectortor (1, 0, ..., 0),
		// and -(1, 0, ..., 0).
		// Thus R v_i simply picks up the ith row of the rotation matrix, up to
		// a sign.
		// This means we don't need any matrix multiplication; R matrix is the
		// list of
		// rotated vectors itself!
		this.vAll = new ArrayList<List<float[]>>(k*l); // random rotation matrices
		
		List<List<float[]>> rotationMatrices = this.vAll;

		for (int i = 0; i < k*l; i++) {
			rotationMatrices.add(i, randomRotation(this.d, r));
		}
	}

	@Override
	public int getDimensionality() {
		return d;
	}

	public static final byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}
	@Override
	public long[] decode(float[] f) {
//		byte[] lg = new byte[this.l * 8];
		long[] dec = Hash(TestUtil.normalize(f));
//		int ct = 0;
//		for (long d:dec) {
//			lg[ct++] = (byte)(d >>> 56);
//			lg[ct++] = (byte)(d >>> 48);
//			lg[ct++] = (byte)(d >>> 40);
//			lg[ct++] = (byte)(d >>> 32);
//			lg[ct++] = (byte)(d >>> 24);
//			lg[ct++] = (byte)(d >>> 16);
//			lg[ct++] = (byte)(d >>> 8 );
//			lg[ct++] = (byte)(d       );
//		}
		return dec;
	}

	@Override
	public float getErrorRadius() {

		return d;
	}

	@Override
	public float getDistance() {
		return distance;
	}

	long argmaxi(float[] p, List<float[]> vs) {
		long maxi = 0;
		float max = 0;

		for (int i = 0; i < this.d; i++) {
			float dot = dot(p, vs.get(i));

			float abs = dot >= 0 ? dot : -dot;
			if (abs < max) {
				continue;
			}

			max = abs;
			maxi = dot >= 0 ? i : i + this.d;
		}
		return maxi;
	}

	float norm(float[] t) {
		float n = 0;
		for (int i = 0; i < t.length; i++) {
			n += t[i] * t[i];
		}
		return (float) Math.sqrt(n);
	}

	float[] scale(float[] t, float s) {
		for (int i = 0; i < t.length; i++) {
			t[i] *= s;
		}
		return t;
	}

	float dot(float[] t, float[] u) {
		float s = 0;
		for (int i = 0; i < t.length; i++) {
			s += t[i] * u[i];
		}
		return s;
	}

	float[] sub(float[] t, float[] u) {
		for (int i = 0; i < t.length; i++) {
			t[i] -= u[i];
		}
		return t;
	}

	float[] random(int d, Random[] r) {

		float[] v = new float[d];

		for (int i = 0; i < d; i++) {
			v[i] = (float) r[i].nextGaussian();
		}
		return v;
	}

	List<float[]> randomRotation(int d, Random[] r2) {
		ArrayList<float[]> R = new ArrayList<>(d);

		for (int i = 0; i < d; i++) {
			R.add(i, random(d, r2));
			float[] u = R.get(i);
			for (int j = 0; j < i; j++) {
				float[] v = R.get(j);
				float vnorm = norm(v);
				if (vnorm == 0) {
					return randomRotation(d, r2);
				}
				float[] vs = new float[v.length];
				System.arraycopy(v, 0, vs, 0, v.length);
				scale(vs, dot(v, u) / vnorm);
				u = sub(u, vs);
			}
			u = scale(u, 1.0f / norm(u));
		}

		return R;
	}

	// Hashes a single point slsh.l times, using a different set of
	// random matrices created and stored by the constructor for each.
	// Stores the result in g to avoid unnecessary allocations.
	//
	// SLSH requires that all vectors lie on a d-dimensional hypershpere,
	// thus having the same norm. Only the Similarity method of FeatureVector
	// is required to take the normalization into account.
	//
	// The complexity of this function is O(nL)
	long[] Hash(float[] p) {
		int ri = 0;
		long h;
		long[] g = new long[this.l];
		for (int i = 0; i < this.l; i++) {
			g[i] = 0;
			for (int j = 0; j < this.k; j++) {
				List<float[]> vs = this.vAll.get(ri);
				h = this.argmaxi(p, vs);
				g[i] |= (h << (this.hbits * j));
				ri++;
			}
		}
		return g;
	}
	

	
	public static void main(String[] args){
		Random r = new Random();
		int d = 64;
		int K = 6; 
		int L = 4;
		Spherical sp = new Spherical(d,K,L);
		for(int i = 0; i<100;i++){
			int ct = 0;
			float distavg = 0.0f;
			for(int j = 0; j<10000;j++){
				float p1[] = new float[d];
				float p2[] = new float[d];
				for(int k = 0;k<d;k++)
				{
					p1[k] = r.nextFloat()*2-1;
					p2[k] = (float) (p1[k]+r.nextGaussian()*((float)i/100f));
				}
				
				distavg+=TestUtil.distance(p1,p2);
				long[] hp1 = sp.Hash(TestUtil.normalize(p1));
				long[] hp2 = sp.Hash(TestUtil.normalize(p2));
				boolean test = false;
				for(int k = 0; k< hp1.length;k++)
				{
					if(hp1[k]==hp2[k]){
						test=true;
						
					}
				}
				if(test)ct++;
			}
			System.out.println(distavg/10000f+"\t"+(float)ct/10000f);
		}
	}

	@Override
	public void setVariance(Float parameterObject) {
		//System.out.println("WARNING: variance is not implemented for spherical normalized data");		
	}

}
