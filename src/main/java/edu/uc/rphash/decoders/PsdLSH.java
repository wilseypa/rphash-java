package edu.uc.rphash.decoders;

import java.util.Random;

import org.apache.commons.math3.distribution.CauchyDistribution;

import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.util.VectorUtil;
import org.apache.commons.math3.distribution.LevyDistribution;

/**
 * Locality-Sensitive Hashing Scheme Based on p-Stable Distributions.
 *
 *
 * For more information on p-stable distribution based LSH, see the following
 * reference.
 *
 * Mayur Datar , Nicole Immorlica , Piotr Indyk , Vahab S. Mirrokni,
 * Locality-sensitive hashing scheme based on p-stable distributions,
 * Proceedings of the twentieth annual symposium on Computational geometry, June
 * 08-11, 2004, Brooklyn, New York, USA.
 */
public class PsdLSH  implements Decoder {
	public static int LEVY = 1;	
	public static int CAUCHY = 1;
	public static int GAUSSIAN = 2;

	int M;// Hash table size
	int L;// Number of hash tables
	int D;// Dimension of the vector, it can be obtained from the instance of
			// Matrix
	int T;// Index mode, you can choose 1(CAUCHY) or 2(GAUSSIAN)
	float W;// Window size
	int bits;

	// Vector<Float>
	float[] rndBs;
	// Vector<Vector<Float>>
	float[][] stableArray;

	public PsdLSH(int M, int L, int D, int T, float W) {
		this.M = M;
		this.L = L;
		this.T = T;
		this.W = W;
		this.D = D;
		bits = (int) Math.ceil(Math.log(M) / Math.log(2));
		rndBs = new float[L];
		stableArray = new float[L][D];
		initialize();
	}
	
	public PsdLSH() {
		M = 512;
		L = 2;
		T = LEVY;
		W = 1f;
		D = 32;
		bits = (int) Math.ceil(Math.log(M) / Math.log(2));
		rndBs = new float[L];
		stableArray = new float[L][D];;
		initialize();
	}

	private void initialize() {

		Random rng = new Random();

		switch (T) {
		case 0:{
			LevyDistribution ld = new LevyDistribution(0,1) ;
			for (int l = 0; l < L; l++) {
				for (int d = 0; d < D; d++) {
					stableArray[l][d] = (float) ld.sample();
				}
				rndBs[l] = rng.nextFloat() * W;

			}
			return;
		}
		
		
		case 1: {
			CauchyDistribution cd = new CauchyDistribution();

			for (int l = 0; l < L; l++) {
				for (int d = 0; d < D; d++) {
					stableArray[l][d] = (float) cd.sample();
				}
				rndBs[l] = rng.nextFloat() * W;

			}
			return;
		}
		case 2: {
			for (int l = 0; l < L; l++) {
				for (int d = 0; d < D; d++) {
					stableArray[l][d] = (float) rng.nextGaussian();
				}
				rndBs[l] = rng.nextFloat() * W;
			}
			return;
		}
		default: {
			return;
		}
		}
	}

	long[] hash(float[] domin) {
		long[] hashVal = new long[L];
		for (int l = 0; l < L; l++) {
			//dot product with stable distribution
			float sum = 0;
			for (int d = 0; d < D; d++) {
				sum += domin[d] * stableArray[l][d];
			}
			hashVal[l] = (long) (Math.floor((sum + rndBs[l]) / W)) % M;
		}
		return hashVal;
	}

	public static void main(String[] args) {
		Random r = new Random();
		int M = 521;
		int L = 5;
		int T = LEVY;
		float W = 5f;
		int d = 32;
		
		
		MurmurHash hash = new MurmurHash(Integer.MAX_VALUE);
		float testResolution = 10000f;
		
		PsdLSH sp = new PsdLSH(M, L, d, T, W);
		for (int i = 0; i < 300; i++) {
			int ct = 0;
			float distavg = 0.0f;
			for (int j = 0; j < testResolution; j++) {
				float p1[] = new float[d];
				float p2[] = new float[d];
				
				//generate a vector
				for (int k = 0; k < d; k++) {
					p1[k] = r.nextFloat() * 2 - 1;
					p2[k] = (float) (p1[k] + r.nextGaussian()
							* ((float) i / 1000f));
				}
				float dist = VectorUtil.distance(p1, p2);
				distavg += dist;
				
				long[] hp1 = sp.hash(p1);
				long[] hp2 = sp.hash(p2);
//				for(int l =0;l<hp1.length-1;l++){
//					if(hp1[l]==hp2[l] && hp1[l+1]==hp2[l+1]){
//						ct++;
//						break;
//					}
//				}
				ct+=(hash.hash(hp2)==hash.hash(hp1))?1:0;
				
			}
			System.out.println(distavg / testResolution + "\t" + (float) ct / testResolution);
		}
	}

	@Override
	public void setVariance(Float parameterObject) {
	}

	@Override
	public int getDimensionality() {
		return D;
	}

	@Override
	public long[] decode(float[] f) {
		return hash(f);
	}

	@Override
	public float getErrorRadius() {
		return 1;
	}

	@Override
	public float getDistance() {
		return 0;
	}

	@Override
	public boolean selfScaling() {
		return true;
	}

}
