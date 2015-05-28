package edu.uc.rphash.decoders;

import java.util.HashSet;
import java.util.Random;

import edu.uc.rphash.tests.TestUtil;

public class E8 implements Decoder {

	int n = 8;
	float[] yt;
	float[] u; 
	float[] v ;

	Dn dn1;
	Dn dn2;
	float distance = -1f;

	float dist(float[] x, float[] y) {
		float sum = 0.0f;
		for (int i = 0; i < x.length; i++)
			sum += ((x[i] - y[i]) * (x[i] - y[i]));
		return (float) Math.sqrt(sum);
	}

	// float[] decodeD8(float[] r) {
	// System.out.println(r[0]+","+r[1]+","+r[2]+","+r[3]+","+r[4]+","+r[5]+","+r[6]+","+r[7]);
	// float[] rInt = { 0, 0, 0, 0, 0, 0, 0, 0 };
	// // float[] rDist = { 0, 0, 0, 0, 0, 0, 0, 0 };
	//
	// int leastreliable = 0;
	// float leastreliabledist = 0.0f;
	// float rDist = 0;
	// for (int i = 0; i < 8; i++) {
	// // round
	// rInt[i] = (int) (r[i] + .5);
	// rDist = Math.abs(r[i] - rInt[i]);
	//
	// if (rDist > leastreliabledist) {
	// leastreliable = i;
	// leastreliabledist = rDist;
	// }
	// }
	// // System.out.println(leastreliable);
	// int sum = (int) (rInt[0] + rInt[1] + rInt[2] + rInt[3] + rInt[4]
	// + rInt[5] + rInt[6] + rInt[7]);
	//
	// // round the wrong way
	// if ((sum & 1) != 0) {
	// if (rInt[leastreliable] > r[leastreliable]) {
	// // System.out.println("b:"+rInt[leastreliable]+":"+r[leastreliable]);
	// rInt[leastreliable] = rInt[leastreliable] - 1;
	// // System.out.println("a:"+rInt[leastreliable]);
	//
	// } else {
	//
	// rInt[leastreliable] = rInt[leastreliable] + 1;
	// // if(rInt[leastreliable]==2)System.out.println("damn");
	//
	// }
	// }

	// //
	// System.out.println(rInt[0]+","+rInt[1]+","+rInt[2]+","+rInt[3]+","+rInt[4]+","+rInt[5]+","+rInt[6]+","+rInt[7]);
	// return Dn.closestPoint(r, 8);
	// }

	public float[] closestPoint(float[] y) {

		dn1.closestPoint(y);
		for (int i = 0; i < n; i++)
			yt[i] = y[i] -.5f;
		dn2.closestPoint(yt);

		if (dn1.distance() < dn2.distance()) {
			distance = dn1.distance();
			System.arraycopy(dn1.getLatticePoint(), 0, v, 0, n);
		} else {
			distance = dn2.distance();
			for (int i = 0; i < n; i++)
				v[i] = dn2.getLatticePoint()[i] + .5f;

		}
		return v;
	}

	// public float[] closestPoint(float[] y) {
	// Dn dn1 = new Dn(8),dn2 = new Dn(8);
	// float dist = 0.0f;
	// float[] yt= new float[8],v = new float[8];
	// dn1.closestPoint(y);
	//
	// for (int i = 0; i < 8; i++)
	// yt[i] = y[i] - .5f;
	//
	// dn2.closestPoint(yt);
	//
	// if (dn1.distance() < dn2.distance()) {
	// dist = dn1.distance();
	// System.arraycopy(dn1.getLatticePoint(), 0, v, 0, 8);
	// } else {
	// dist = dn2.distance();
	// for (int i = 0; i < 8; i++)
	// v[i] = dn2.getLatticePoint()[i] + .5f;
	// }
	// return v;
	// }
	// byte[] decodeE8(float[] r) {
	//
	// float[] nr = decodeD8(r);// normal
	//
	//
	// nr = decodeD8( r);
	//
	// float[] hr = Arrays.copyOf(r, 8);
	// for (int i = 0; i < 8; i++) {
	// hr[i] -= .5f;
	// }
	// hr = decodeD8( hr);
	// for (int i = 0; i < 8; i++) {
	// hr[i] += .5f;
	// }
	// float nrd = dist(nr, r);
	// float hrd = dist(hr, r);
	//
	// // our range is 0-2, so normalized to 2
	// if (hrd < nrd)
	// nr = hr;
	// // float[] nr = closestPoint(r);
	// byte[] ret = new byte[8];
	// for (int i = 0; i < 8; i++)
	// ret[i] = (byte) (nr[i] );
	//
	// return ret;
	// }

	// int decodeE8Int(float[] r) {
	// int s = 0;
	// byte[] nr = decodeE8(r);
	//
	// for (int i = 0; i < 8; i++) {
	// s += nr[i];
	// s <<= 2;
	// }
	//
	// return s;
	// }

	@Override
	public int getDimensionality() {
		return 8;
	}

	public static long hash(float[] s) {
		long ret = (int) s[0];
		for (int i = 1; i < s.length; i++) {
			ret <<= 8;
			ret += (int) (2 * ((s[i] + 2)));

		}
		return ret;
	}
	float variance;
	public E8(float var){
		int n = 8;
		yt = new float[8];
		u = new float[8];
		v = new float[8];
		dn1 = new Dn(8);
		dn2 = new Dn(8);
		this.variance = var;
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
		float[] tmp = new float[8];
		float sclr = 1f/variance;
		for (int i = 0; i < 8; i++)
			tmp[i] = f[i]*sclr;
		tmp = closestPoint(tmp);
		long[] tmp2 = new long[8];
		for (int i = 0; i < 8; i++)
			tmp2[i] = (byte) ((2 + tmp[i]*sclr) * 2);

		return tmp2;
	}

	@Override
	public float getErrorRadius() {

		return (float) (2 * Math.sqrt(2) / 3);
	}

//	public static void main(String[] args) {
//		E8 e8 = new E8(1f);
//
//		byte[] hr = e8.decode(new float[] { 1.2f, 1.2f, 1.2f, 1.2f, 1.2f, 1.1f,
//				1.8f, 1.4f });
//		System.out.println(hr[0] + "," + hr[1] + "," + hr[2] + "," + hr[3]
//				+ "," + hr[4] + "," + hr[5] + "," + hr[6] + "," + hr[7]);
//		Random r = new Random();
//		HashSet<Long> s = new HashSet<Long>();
//		float[] x = new float[8];
//		for (int i = 0; i < 10000000; i++) {
//			for (int j = 0; j < 8; j++)
//				x[j] = r.nextFloat() * 2.f - 1f;
//			s.add(hash(e8.closestPoint(x)));
//		}
//		System.out.println(s.size());
//	}

	@Override
	public float getDistance() {
		
		return distance;
	}
	
	public static void main(String[] args) {
		Random r = new Random();
		int d = 8;
		int K = 6;
		int L = 2;
		E8 sp = new E8(1f);
		for (int i = 0; i < 100; i++) {
			int ct = 0;
			float distavg = 0.0f;
			for (int j = 0; j < 10000; j++) {
				float p1[] = new float[d];
				float p2[] = new float[d];
				for (int k = 0; k < d; k++) {
					p1[k] = r.nextFloat() * 2 - 1;
					p2[k] = (float) (p1[k] + r.nextGaussian()
							* ((float) i / 200f));
				}

				distavg+=TestUtil.distance(p1,p2);
				long[] hp1 = sp.decode(TestUtil.normalize(p1));
				long[] hp2 = sp.decode(TestUtil.normalize(p2));
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
}
