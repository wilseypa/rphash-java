package edu.uc.rphash.decoders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.util.VectorUtil;

public class E8 implements Decoder {

	int n = 8;
	float[] yt;
	float[] u;
	byte[] v;

	Dn dn1;
	Dn dn2;
	float distance = -1f;

	float dist(float[] x, float[] y) {
		float sum = 0.0f;
		for (int i = 0; i < x.length; i++)
			sum += ((x[i] - y[i]) * (x[i] - y[i]));
		return (float) Math.sqrt(sum);
	}

	// HashMap<Float,Integer> ctmap = new HashMap<Float,Integer>();
	public byte[] closestPoint(float[] y) {
		dn1.closestPoint(y);

		for (int i = 0; i < n; i++)
			yt[i] = y[i] - .5f;

		dn2.closestPoint(yt);

		if (dn1.distance() < dn2.distance()) {
			distance = dn1.distance();
			for (int i = 0; i < n; i++) {
				v[i] = (byte) (4 + dn2.getLatticePoint()[i] * 2);
			}
		} else {
			distance = dn2.distance();
			for (int i = 0; i < n; i++) {
				v[i] = (byte) (4 + 2 * (dn2.getLatticePoint()[i] + .5f));
			}
		}
		return v;
	}

	@Override
	public int getDimensionality() {
		return 8;
	}

	final float variance;

	public E8(float var) {
		int n = 8;
		yt = new float[n];
		u = new float[n];
		v = new byte[n];
		dn1 = new Dn(n);
		dn2 = new Dn(n);
		this.variance = 1.0f;// var;
	}

	@Override
	/**
	 * this decoder uses the D8 partition to decode E8 lattice it returns an
	 * integer label for the lattice point
	 * 
	 * @param r
	 * @return
	 */
	public long[] decode(float[] f) {

		byte[] tmp = closestPoint(f);

		long tmp2 = tmp[0];
		tmp2 += tmp[1] << 3;
		tmp2 += tmp[2] << 6;
		tmp2 += tmp[3] << 9;
		tmp2 += tmp[4] << 12;
		tmp2 += tmp[5] << 15;
		tmp2 += tmp[6] << 18;
		tmp2 += tmp[7] << 21;

		return new long[] { tmp2 };
	}

	@Override
	public float getErrorRadius() {

		return (float) (2 * Math.sqrt(2) / 3);
	}

	// public static void main(String[] args) {
	// E8 e8 = new E8(1f);
	//
	// byte[] hr = e8.decode(new float[] { 1.2f, 1.2f, 1.2f, 1.2f, 1.2f, 1.1f,
	// 1.8f, 1.4f });
	// System.out.println(hr[0] + "," + hr[1] + "," + hr[2] + "," + hr[3]
	// + "," + hr[4] + "," + hr[5] + "," + hr[6] + "," + hr[7]);
	// Random r = new Random();
	// HashSet<Long> s = new HashSet<Long>();
	// float[] x = new float[8];
	// for (int i = 0; i < 10000000; i++) {
	// for (int j = 0; j < 8; j++)
	// x[j] = r.nextFloat() * 2.f - 1f;
	// s.add(hash(e8.closestPoint(x)));
	// }
	// System.out.println(s.size());
	// }

	@Override
	public float getDistance() {

		return distance;
	}

	public static void main(String[] args) {
		Random r = new Random();
		int d = 8;// 24;

		E8 sp = new E8(1f);
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

	@Override
	public void setVariance(Float parameterObject) {

	}

	@Override
	public boolean selfScaling() {
		return false;
	}
}
