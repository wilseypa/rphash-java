package edu.uc.rphash.decoders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.tests.TestUtil;

public class PStableDistribution implements Decoder {

	int m;
	int k;
	int dimensionality;
	float distance;
	float[][] mvec;
	int[][] index;

	public PStableDistribution(int dimensionality, int k, int m) {
		this.dimensionality = dimensionality;
		this.m = m;
		this.k = k;
		this.mvec = new float[m][k];
		this.index = new int[m][k];

		Random r = new Random();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < k; j++) {
				this.index[i][j] = r.nextInt(dimensionality);
				this.mvec[i][j] = (float) r.nextGaussian();
			}
		}
	}

	public PStableDistribution(float f) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getDimensionality() {

		return dimensionality;
	}

	@Override
	public long[] decode(float[] f) {
		int byteindex = 0;
		byte curbyte = 0;
		long[] ret = new long[(int) Math.ceil(k * m )];
		int bytes = 0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < k; j++) {
				if (f[this.index[i][j]] < this.mvec[i][j])
					curbyte += 1;
				curbyte <<= 1;
				byteindex++;
				if (byteindex == 7) {
					byteindex = 0;
					ret[bytes++] = curbyte;
				}
			}
		}
		ret[bytes] = curbyte;

		return ret;
	}

	@Override
	public float getErrorRadius() {

		return dimensionality;
	}

	@Override
	public float getDistance() {

		return distance;
	}

	public static void main(String[] args) {
		Random r = new Random();
		int d = 64;
		int K = 6;
		int L = 2;
		PStableDistribution sp = new PStableDistribution(64,K,L);
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

	@Override
	public void setVariance(Float parameterObject) {
		// TODO Auto-generated method stub
		
	}
}
