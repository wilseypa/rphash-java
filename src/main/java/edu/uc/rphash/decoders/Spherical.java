package edu.uc.rphash.decoders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.util.VectorUtil;

/**
 * Spherical LSH Decoder based on SLSH (lgpl)
 * 
 * @author lee
 *
 */
public class Spherical implements Decoder {
	int HashBits = 64;
	final List<List<float[]>> vAll; // vAll[i][j] is the vector $A_i \tilde v_j$
									// from
	// the article.
	int hbits; // Ceil(Log2(2*d)).
	int d; // the dimension of the feature space.
	int k; // number of elementary hash functions (h) to be concatenated to
			// obtain a reliable enough hash function (g). LSH queries becomes
			// more selective with increasing k, due to the reduced the
			// probability of collision.
	int l; // number of "copies" of the bins (with a different random matrices).
			// Increasing L will increase the number of points the should be
			// scanned linearly during query.
	float distance = 0;

	/**
	 * This class represent a spherical lsh scheme. Vectors are decoded to the
	 * nearest vertex of the d dimensional orthoplex reresented by a canonical
	 * ordered integer.
	 * 
	 * @param d
	 *            - the number of dimension in the orthoplex
	 * @param k
	 *            - number of rotations of the fundamental hash functions
	 * @param L
	 *            - the number to search, currently ignored in RPHash
	 */
	public Spherical(int d, int k, int L) {
		this.d = d;// number of dimensions
		this.k = k;// number of elementary hash functions
		this.l = 1;// L;//number of copies to search
		double nvertex = 2.0 * this.d;
		this.hbits = (int) Math.ceil(Math.log(nvertex) / Math.log(2));
		int kmax = (int) (HashBits / this.hbits);
		if (this.k > kmax) {
			this.k = kmax;
			System.out
					.printf("k is too big, chopping down (%d->%d)\n", k, kmax);
		}

		Random[] r = new Random[d];
		for (int i = 0; i < d; i++)
			r[i] = new Random();

		// For orthoplex, the basis Vectortors v_i are permutations of the
		// Vectortor (1, 0, ..., 0),
		// and -(1, 0, ..., 0).
		// Thus R v_i simply picks up the ith row of the rotation matrix, up to
		// a sign.
		// This means we don't need any matrix multiplication; R matrix is the
		// list of
		// rotated vectors itself!
		this.vAll = new ArrayList<List<float[]>>(k * l); // random rotation
															// matrices
		for (int i = 0; i < k * l; i++) {
			this.vAll.add(i, randomRotation(this.d, r));
		}
	}

	@Override
	public int getDimensionality() {
		return d;
	}

	@Override
	public long[] decode(float[] f) {
		long dec = Hash(f);
		return new long[] { dec };
	}

	@Override
	public float getErrorRadius() {
		return d;
	}

	@Override
	public float getDistance() {
		return distance;
	}

	long argmaxi(float[] p, int index) {
		List<float[]> vs = vAll.get(index);
		long maxi = 0;
		float max = 0;
		for (int i = 0; i < this.d; i++) {
			float dot = dot(p, vs.get(i));
			// compute orthoplex of -1 and 1 simultaneously
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
	long Hash(float[] p) {
		int ri = 0;
		long h = 0;
//		float normp = norm(p);
//		p = scale(p, 1.0f / normp);
		for (int i = 0; i < this.l; i++) {
			for (int j = 0; j < this.k; j++) {
				h = h | this.argmaxi(p, ri);
				h <<= this.hbits;
				ri++;
			}
		}
		return h ;//+ (int) (normp);

	}

	public static void main(String[] args) {
		Random r = new Random();
		int d = 64;
		int K = 2;
		int L = 1;
		Spherical sp = new Spherical(d, K, L);

		// MultiDecoder sp = new MultiDecoder( d, e8);
		MurmurHash hash = new MurmurHash(Integer.MAX_VALUE);
		float testResolution = 10000f;

		HashMap<Long, Integer> ctmap = new HashMap<Long, Integer>();

		for (int i = 0; i < 400; i++) {
			int ct = 0;
			float distavg = 0.0f;
			for (int j = 0; j < testResolution; j++) {
				float p1[] = new float[d];
				float p2[] = new float[d];

				// generate a vector
				for (int k = 0; k < d; k++) {
					p1[k] = r.nextFloat() * 2 - 1f;
					p2[k] = (float) (p1[k] + r.nextGaussian()
							* ((float) i / 1000f));
				}
				float dist = VectorUtil.distance(p1, p2);
				distavg += dist;
				long[] l1 = sp.decode(p1);
				long[] l2 = sp.decode(p2);

				ctmap.put(l1[0],
						ctmap.containsKey(l1[0]) ? 1 + ctmap.get(l1[0]) : 1);

				long hp1 = hash.hash(l1);
				long hp2 = hash.hash(l2);

				// ctmap.put(hp1,ctmap.containsKey(hp1)?1+ctmap.get(hp1):1);

				ct += (hp2 == hp1) ? 1 : 0;

			}

			System.out.println(distavg / testResolution + "\t" + (float) ct
			/ testResolution);
		}
	}

	float variance = 1f;

	@Override
	public void setVariance(Float parameterObject) {
		variance = parameterObject;
	}

	@Override
	public boolean selfScaling() {
		return true;
	}

}
