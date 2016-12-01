package edu.uc.rphash.standardhash;

/*
 * 
 * CrapWow 32 Bit from https://github.com/sunnygleason/g414-hash
 */
public class CrapWow implements HashAlgorithm {
	int tablesize;

	public CrapWow(int tablesize) {
		this.tablesize = tablesize;
	}

	@Override
	public long hash(long d) {
		byte[] s2 = new byte[8];
		int ct = 0;

		s2[ct++] = (byte) (d >>> 56);
		s2[ct++] = (byte) (d >>> 48);
		s2[ct++] = (byte) (d >>> 40);
		s2[ct++] = (byte) (d >>> 32);
		s2[ct++] = (byte) (d >>> 24);
		s2[ct++] = (byte) (d >>> 16);
		s2[ct++] = (byte) (d >>> 8);
		s2[ct++] = (byte) (d);

		return computeCWowIntHash(s2, 0) % tablesize;
	}

	@Override
	public long hash(long[] s) {
		byte[] s2 = new byte[s.length * 8];
		int ct = 0;
		for (long d : s) {
			s2[ct++] = (byte) (d >>> 56);
			s2[ct++] = (byte) (d >>> 48);
			s2[ct++] = (byte) (d >>> 40);
			s2[ct++] = (byte) (d >>> 32);
			s2[ct++] = (byte) (d >>> 24);
			s2[ct++] = (byte) (d >>> 16);
			s2[ct++] = (byte) (d >>> 8);
			s2[ct++] = (byte) (d);
		}
		return computeCWowIntHash(s2, 0) % tablesize;
	}

	public final static int CWOW_32_M = 0x57559429;
	public final static int CWOW_32_N = 0x5052acdb;
	public static final long LONG_LO_MASK = 0x00000000FFFFFFFFL;

	/** gather an int from the specified index into the byte array */
	public static final int gatherIntLE(byte[] data, int index) {
		int i = data[index] & 0xFF;
		i |= (data[++index] & 0xFF) << 8;
		i |= (data[++index] & 0xFF) << 16;
		i |= (data[++index] << 24);
		return i;
	}

	public static final int gatherPartialIntLE(byte[] data, int index,
			int available) {
		int i = data[index] & 0xFF;
		if (available > 1) {
			i |= (data[++index] & 0xFF) << 8;
			if (available > 2) {
				i |= (data[++index] & 0xFF) << 16;
			}
		}
		return i;
	}

	public int computeCWowIntHash(byte[] data, int seed) {
		final int length = data.length;
		/* cwfold( a, b, lo, hi ): */
		/* p = (u32)(a) * (u64)(b); lo ^=(u32)p; hi ^= (u32)(p >> 32) */
		/* cwmixa( in ): cwfold( in, m, k, h ) */
		/* cwmixb( in ): cwfold( in, n, h, k ) */
		int hVal = seed;
		int k = length + seed + CWOW_32_N;
		long p = 0;
		int pos = 0;
		int len = length;
		while (len >= 8) {
			int i1 = gatherIntLE(data, pos);
			int i2 = gatherIntLE(data, pos + 4);
			/* cwmixb(i1) = cwfold( i1, N, hVal, k ) */
			p = i1 * (long) CWOW_32_N;
			k ^= p & LONG_LO_MASK;
			hVal ^= (p >> 32);
			/* cwmixa(i2) = cwfold( i2, M, k, hVal ) */
			p = i2 * (long) CWOW_32_M;
			hVal ^= p & LONG_LO_MASK;
			k ^= (p >> 32);
			pos += 8;
			len -= 8;
		}
		if (len >= 4) {
			int i1 = gatherIntLE(data, pos);
			/* cwmixb(i1) = cwfold( i1, N, hVal, k ) */
			p = i1 * (long) CWOW_32_N;
			k ^= p & LONG_LO_MASK;
			hVal ^= (p >> 32);
			pos += 4;
			len -= 4;
		}
		if (len > 0) {
			int i1 = gatherPartialIntLE(data, pos, len);
			/* cwmixb(i1) = cwfold( i1, N, hVal, k ) */
			p = (i1 & ((1 << (len * 8)) - 1)) * (long) CWOW_32_M;
			hVal ^= p & LONG_LO_MASK;
			k ^= (p >> 32);
		}
		p = (hVal ^ (k + CWOW_32_N)) * (long) CWOW_32_N;
		k ^= p & LONG_LO_MASK;
		hVal ^= (p >> 32);
		hVal ^= k;
		return hVal;
	}

}
