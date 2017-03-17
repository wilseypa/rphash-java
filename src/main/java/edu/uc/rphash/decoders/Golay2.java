package edu.uc.rphash.decoders;

import java.util.HashMap;
import java.util.Random;

import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.util.VectorUtil;

/* Encoding matrix, H

 These entries are formed from the matrix specified in H.223/B.3.2.1.3;
 it's first transposed so we have:

 [P1 ]   [111110010010]  [MC1 ]
 [P2 ]   [011111001001]  [MC2 ]
 [P3 ]   [110001110110]  [MC3 ]
 [P4 ]   [011000111011]  [MC4 ]
 [P5 ]   [110010001111]  [MPL1]
 [P6 ] = [100111010101]  [MPL2]
 [P7 ]   [101101111000]  [MPL3]
 [P8 ]   [010110111100]  [MPL4]
 [P9 ]   [001011011110]  [MPL5]
 [P10]   [000101101111]  [MPL6]
 [P11]   [111100100101]  [MPL7]
 [P12]   [101011100011]  [MPL8]

 So according to the equation, P1 = MC1+MC2+MC3+MC4+MPL1+MPL4+MPL7

 Looking down the first column, we see that if MC1 is set, we toggle bits
 1,3,5,6,7,11,12 of the parity: in binary, 110001110101 = 0xE3A

 Similarly, to calculate the inverse, we read across the top of the table and
 see that P1 is affected by bits MC1,MC2,MC3,MC4,MPL1,MPL4,MPL7: in binary,
 111110010010 = 0x49F.

 I've seen cunning implementations of this which only use one table. That
 technique doesn't seem to work with these numbers though.
 */

public class Golay2 {
	static int[] golay_encode_matrix = { 0xC75, 0x49F, 0xD4B, 0x6E3, 0x9B3,
			0xB66, 0xECC, 0x1ED, 0x3DA, 0x7B4, 0xB1D, 0xE3A, };

	static int[] golay_decode_matrix = { 0x49F, 0x93E, 0x6E3, 0xDC6, 0xF13,
			0xAB9, 0x1ED, 0x3DA, 0x7B4, 0xF68, 0xA4F, 0xC75, };

	/* Function to compute the Hamming weight of a 12-bit integer */
	static int weight12(int vector) {
		int w = 0;
		int i;
		for (i = 0; i < 12; i++)
			if ((vector & 1 << i) == 1)
				w++;
		return w;
	}

	/* returns the golay coding of the given 12-bit word */
	static int golay_coding(int w) {
		int out = 0;
		int i;

		for (i = 0; i < 12; i++) {
			if ((w & 1 << i) == 1)
				out ^= golay_encode_matrix[i];
		}
		return out;
	}

	/* encodes a 12-bit word to a 24-bit codeword */
	int golay_encode(int w) {
		return ((int) w) | ((int) golay_coding(w)) << 12;
	}

	/* returns the golay coding of the given 12-bit word */
	static int golay_decoding(int w) {
		int out = 0;
		int i;

		for (i = 0; i < 12; i++) {
			if ((w & 1 << (i)) == 1)
				out ^= golay_decode_matrix[i];
		}
		return out;
	}

	/*
	 * return a mask showing the bits which are in error in a received 24-bit
	 * codeword, or -1 if 4 errors were detected.
	 */
	int golay_errors(int codeword) {
		int received_data, received_parity;
		int syndrome;
		int w, i;
		int inv_syndrome = 0;

		received_parity = (int) (codeword >>> 12);
		received_data = (int) (codeword & 0xfff);

		/*
		 * We use the C notation ^ for XOR to represent addition modulo 2.
		 * 
		 * Model the received codeword (r) as the transmitted codeword (u) plus
		 * an error vector (e).
		 * 
		 * r = e ^ u
		 * 
		 * Then we calculate a syndrome (s):
		 * 
		 * s = r * H, where H = [ P ], where I12 is the identity matrix [ I12 ]
		 * 
		 * (In other words, we calculate the parity check for the received data
		 * bits, and add them to the received parity bits)
		 */

		syndrome = (int) (received_parity ^ (golay_coding(received_data)));
		w = weight12(syndrome);

		/*
		 * The properties of the golay code are such that the Hamming distance
		 * (ie, the minimum distance between codewords) is 8; that means that
		 * one bit of error in the data bits will cause 7 errors in the parity
		 * bits.
		 * 
		 * In particular, if we find 3 or fewer errors in the parity bits,
		 * either: - there are no errors in the data bits, or - there are at
		 * least 5 errors in the data bits we hope for the former (we don't
		 * profess to deal with the latter).
		 */
		if (w <= 3) {
			return ((int) syndrome) << 12;
		}

		/*
		 * the next thing to try is one error in the data bits. we try each bit
		 * in turn and see if an error in that bit would have given us anything
		 * like the parity bits we got. At this point, we tolerate two errors in
		 * the parity bits, but three or more errors would give a total error
		 * weight of 4 or more, which means it's actually uncorrectable or
		 * closer to another codeword.
		 */

		for (i = 0; i < 12; i++) {
			int error = (int) (1 << i);
			int coding_error = golay_encode_matrix[i];
			if (weight12((int) (syndrome ^ coding_error)) <= 2) {
				return (int) ((((int) (syndrome ^ coding_error)) << 12) | (int) error);
			}
		}

		/*
		 * okay then, let's see whether the parity bits are error free, and all
		 * the errors are in the data bits. model this as follows:
		 * 
		 * [r | pr] = [u | pu] + [e | 0]
		 * 
		 * pr = pu pu = H * u => u = H' * pu = H' * pr , where H' is inverse of
		 * H
		 * 
		 * we already have s = H*r + pr, so pr = s - H*r = s ^ H*r e = u ^ r =
		 * (H' * ( s ^ H*r )) ^ r = H'*s ^ r ^ r = H'*s
		 * 
		 * Once again, we accept up to three error bits...
		 */

		inv_syndrome = golay_decoding(syndrome);
		w = weight12(inv_syndrome);
		if (w <= 3) {
			return (int) inv_syndrome;
		}

		/*
		 * Final shot: try with 2 errors in the data bits, and 1 in the parity
		 * bits; as before we try each of the bits in the parity in turn
		 */
		for (i = 0; i < 12; i++) {
			int error = (int) (1 << i);
			int coding_error = golay_decode_matrix[i];
			if (weight12((int) (inv_syndrome ^ coding_error)) <= 2) {
				int error_word = ((int) (inv_syndrome ^ coding_error))
						| ((int) error) << 12;
				return (int) error_word;
			}
		}

		/* uncorrectable error */
		return -1;
	}

	/*
	 * decode a received codeword. Up to 3 errors are corrected for; 4 errors
	 * are detected as uncorrectable (return -1); 5 or more errors cause an
	 * incorrect correction.
	 */
	public int golay_decode(int w) {
		int data = (int) (w & 0xfff);
		int errors = golay_errors(w);
		int data_errors;

		if (errors == -1)
			return -1;
		data_errors = (int) (errors & 0xfff);
		return (int) (data ^ data_errors);
	}
	
	public static void main(String[] args) {
		Random r = new Random();
		Golay2 sp = new Golay2();


		for (int i = 0; i < 400; i++) {
			int v = r.nextInt()&0xfff;
			int errors = r.nextInt(4)+1;
			System.out.printf("%d,%d=",errors,v);
			v = sp.golay_encode(v);
			for(int j = 0;j<errors;j++){
				int errbit = 1<<r.nextInt(23);
				v ^=errbit; 
			}
			System.out.printf("%d\n",sp.golay_decode(v));
		}
	}

}
