package edu.uc.rphash.decoders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
	public byte[] decode(float[] f) {
		int byteindex = 0;
		byte curbyte = 0;
		byte[] ret = new byte[(int) Math.ceil(k * m / 8)];
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

}
