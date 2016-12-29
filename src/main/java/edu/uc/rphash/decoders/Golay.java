package edu.uc.rphash.decoders;

import java.util.Arrays;
import java.util.Random;

import edu.uc.rphash.frequentItemSet.Countable;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.util.VectorUtil;

public class Golay implements Decoder{
	        /**
	         * Utility methods that converts a binary string into and int.
	         * 
	         * @param str a string containing a binary number
	         * 
	         * @return the numeric value of the supplied string
	         */
	        
	    private static int fromBinary(final String str) {
	        return Integer.parseInt(str, 2);
	    }

	    /**
	     * Mask that preserves the last 12 bits (bits in dataword).
	     */
	    
	    private static final int MASK = 0xfff; //== fromBinary("111111111111");
	    
	    /**
	     * Generator matrix for the code, multiplied with a dataword to generate a codeword.
	     */
	    
	    private static final int[] sGenerator = {

	            fromBinary("100000000000"),
	            fromBinary("010000000000"),
	            fromBinary("001000000000"),
	            fromBinary("000100000000"),
	            fromBinary("000010000000"),
	            fromBinary("000001000000"),
	            fromBinary("000000100000"),
	            fromBinary("000000010000"),
	            fromBinary("000000001000"),
	            fromBinary("000000000100"),
	            fromBinary("000000000010"),
	            fromBinary("000000000001"),
	         
	            /* ALTERNATIVE MATRIX - UNUSED
	            fromBinary("110111000101"),
	            fromBinary("101110001011"),
	            fromBinary("011100010111"),
	            fromBinary("111000101101"),
	            fromBinary("110001011011"),
	            fromBinary("100010110111"),
	            fromBinary("000101101111"),
	            fromBinary("001011011101"),
	            fromBinary("010110111001"),
	            fromBinary("101101110001"),
	            fromBinary("011011100011"),
	            fromBinary("111111111110"),
	            */
	            
	            fromBinary("011111111111"),
	            fromBinary("111011100010"),
	            fromBinary("110111000101"),
	            fromBinary("101110001011"),
	            fromBinary("111100010110"),
	            fromBinary("111000101101"),
	            fromBinary("110001011011"),
	            fromBinary("100010110111"),
	            fromBinary("100101101110"),
	            fromBinary("101011011100"),
	            fromBinary("110110111000"),
	            fromBinary("101101110001"),
	    };

	    /**
	     * Transpose of the generator matrix, multiplied with a codeword to generate a syndrome.
	     */
	    
	    private static final int[] sCheck = {

	        fromBinary("011111111111100000000000"),
	        fromBinary("111011100010010000000000"),
	        fromBinary("110111000101001000000000"),
	        fromBinary("101110001011000100000000"),
	        fromBinary("111100010110000010000000"),
	        fromBinary("111000101101000001000000"),
	        fromBinary("110001011011000000100000"),
	        fromBinary("100010110111000000010000"),
	        fromBinary("100101101110000000001000"),
	        fromBinary("101011011100000000000100"),
	        fromBinary("110110111000000000000010"),
	        fromBinary("101101110001000000000001"),

	    };

	    /**
	     * A 4096 (2^12) element array that maps datawords to codewords.
	     */
	    
	    private static final int[] sCodewords;
	    
	    /**
	     * A 4096 (2^12) element array that maps syndromes to error bits.
	     */
	    
	    private static final int[] sErrors;
	    
	     //static initialization
	    static {
	        sCodewords = computeCodewords();
	        sErrors = computeErrors();
	    }

	    /**
	     * Generates the codewords array.
	     * 
	     * @return an array for assignment to {@link sCodewords}
	     */
	    
	    private static int[] computeCodewords() {
	        int[] cws = new int[4096];
	        //iterate over all valid datawords
	        for (int i = 0; i < 4096; i++) {
	                //multiply dataword by generator matrix
	            int cw = 0;
	            for (int j = 0; j < 24; j++) {
	                int d = i & sGenerator[j];
	                int p = Integer.bitCount(d);
	                cw = (cw << 1) | (p & 1);
	            }
	            //store resulting codeword
	            cws[i] = cw;
	        }
	        return cws;
	    }
	    
	    /**
	     * Generates error array.
	     * 
	     * @return an array for assignment to {@link sErrors}
	     */
	    
	    private static int[] computeErrors() {
	        int[] errors = new int[4096];
	        //fill array with -1 (indicates that error cannot be corrected
	        Arrays.fill(errors, -1);

	        //record syndrome for zero error (valid) word
	        {
	                int error = 0;
	                int syn = syndrome(error);
	                errors[syn] = error;
	        }
	        
	        //record syndrome for each single error word
	        for (int i = 0; i < 24; i++) {
	                int error = 1 << i;
	                int syn = syndrome(error);
	                errors[syn] = error;
	        }
	        
	        //record syndrome for each double error word
	        for (int i = 1; i < 24; i++) {
	            for (int j = 0; j < i; j++) {
	                int error = (1 << i) | (1 << j);
	                int syn = syndrome(error);
	                errors[syn] = error;
	            }
	        }
	        
	        //record syndrome for each triple error word
	        for (int i = 2; i < 24; i++) {
	            for (int j = 1; j < i; j++) {
	                for (int k = 0; k < j; k++) {
	                    int error = (1 << i) | (1 << j) | (1 << k);
	                        int syn = syndrome(error);
	                        errors[syn] = error;
	                }
	            }
	        }

	        //code can't resolve quadruple errors
	        return errors;
	    }
	    
	    /**
	     * Encodes a 12 bit data word into a codeword. The 12 bits must be in the
	     * least significant positions and all other supplied bits must be zero.
	     * 
	     * @param data a 12 bit data word
	     * @return the 24 bit code word
	     */
	    
	    public static int encode(final int data) {
	        return sCodewords[data];
	    }
	    
	    /**
	     * Computes the syndrome for the supplied codeword. The 24 bits must be in
	     * the least significant positions.
	     * 
	     * @param word a candidate code word
	     * @return the syndrome for the supplied word
	     */
	    
	    public static int syndrome(final int word) {
	        //multiply codeword by the check matrix
	        int syndrome = 0;
	        for (int j = 0; j < 12; j++) {
	            int d = word & sCheck[j];
	            int p = Integer.bitCount(d);
	            syndrome = (syndrome << 1) | (p & 1);
	        }
	        return syndrome;
	    }

	    /**
	     * Whether the supplied candidate code word is a valid code word. The 24
	     * bits must be in the least significant positions and all other supplied
	     * bits must be zero.
	     * 
	     * @param word the candidate code word
	     * @return true iff the supplied word is a valid codeword
	     */
	    
	    public static boolean isCodeword(final int word) {
	        //optimization - is it worth it?
	        int w = Integer.bitCount(word);
	        if (w != 0 && w != 8 && w != 12 && w != 16 && w != 24) return false;
	        return syndrome(word) == 0;
	    }
	    
	    /**
	     * Decodes a valid code word into a dataword.
	     * 
	     * @param codeword a valid code word
	     * @return the corresponding data word
	     */
	    public static int decodeWord(final int codeword) {
	        return (codeword >> 12) & MASK;
	    }

	    /**
	     * Attempts to correct and decode a codeword. The 24 bits must be in the
	     * least significant positions and all other supplied bits must be zero.
	     * NOTE: for codewords with four errors, this method does not attempt any correction
	     * 
	     * @param word a word to be decoded
	     * @return a decoded and possibly corrected data word
	     */
	    
	    public static int correctAndDecode(final int word) {
	        int err = sErrors[ syndrome(word) ];
	        //for 4 errors we currently just give up!!
	        return err <= 0 ? decodeWord(word) : decodeWord(word ^ err);
	    }

		private float[] variance;
	 
	    // constructor
	    
	    /**
	     * Cannot be instantiated.
	     */
	    
	    public Golay() { }
	    
	    
	    public static void main(String[] args) {
			Random r = new Random();
			int d = 24;
	
			Golay sp = new Golay();
			MurmurHash hash = new MurmurHash(Integer.MAX_VALUE);
			float testResolution = 10000f;

			for (int i = 0; i < 300; i++) {
				int ct = 0;
				float distavg = 0.0f;
				for (int j = 0; j < testResolution; j++) {
					float p1[] = new float[d];
					float p2[] = new float[d];

					// generate a vector
					for (int k = 0; k < d; k++) {
						p1[k] = r.nextFloat() * 2 - 1;
						p2[k] = (float) (p1[k] + r.nextGaussian()
								* ((float) i / 1000f));
					}
					float dist = VectorUtil.distance(p1, p2);
					distavg += dist;

					long hp1 = hash.hash(sp.decode(p1));
					long hp2 = hash.hash(sp.decode(p2));

					ct+=(hp2==hp1)?1:0;

				}
				System.out.println(distavg / testResolution + "\t" + (float) ct
						/ testResolution);
			}
	}
	    
//	    float varTot = 1.0f;
	    @Override
		public long[] decode(float[] p1) {
			int codeword = 0;
			if(p1[0]>0)codeword+=1;
			for(int i=1;i<24;i++){
				codeword<<=1;
				if(p1[i]>0)codeword+=1;
			}
			return new long[]{correctAndDecode(codeword)};
		}

//		@Override
//		public void setVariance(float[] parameterObject) {
//			variance = parameterObject;
//			for(int i = 0 ; i<this.getDimensionality();i++)varTot+=this.variance[i];
//			varTot/=(float)this.getDimensionality();
//		}

		@Override
		public int getDimensionality() {
			return 24;
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

		@Override
		public void setCounter(Countable counter) {
			// TODO Auto-generated method stub
			
		}
}
