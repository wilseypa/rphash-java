package edu.uc.rphash.standardhash;

public class MurmurHash implements HashAlgorithm {
	
	final int seed =  216613626;
	long tablesize;
	
	public MurmurHash(long tablesize){
		this.tablesize = tablesize;
	}
	
	
	@Override			
	  public long hash(long[] data1) {
		    
			byte[] data = new byte[data1.length*8];
			int ct = 0;
			for (long d:data1) {
				data[ct++] = (byte)(d >>> 56);
				data[ct++] = (byte)(d >>> 48);
				data[ct++] = (byte)(d >>> 40);
				data[ct++] = (byte)(d >>> 32);
				data[ct++] = (byte)(d >>> 24);
				data[ct++] = (byte)(d >>> 16);
				data[ct++] = (byte)(d >>> 8 );
				data[ct++] = (byte)(d       );
			}
		
		
		
		
		
			int m = 0x5bd1e995;
		    int r = 24;

		    int h = seed ^ data.length;

		    int len = data.length;
		    int len_4 = len >> 2;

		    for (int i = 0; i < len_4; i++) {
		      int i_4 = i << 2;
		      int k = data[i_4 + 3];
		      k = k << 8;
		      k = k | (data[i_4 + 2] & 0xff);
		      k = k << 8;
		      k = k | (data[i_4 + 1] & 0xff);
		      k = k << 8;
		      k = k | (data[i_4 + 0] & 0xff);
		      k *= m;
		      k ^= k >>> r;
		      k *= m;
		      h *= m;
		      h ^= k;
		    }

		    int len_m = len_4 << 2;
		    int left = len - len_m;

		    if (left != 0) {
		      if (left >= 3) {
		        h ^= (int) data[len - 3] << 16;
		      }
		      if (left >= 2) {
		        h ^= (int) data[len - 2] << 8;
		      }
		      if (left >= 1) {
		        h ^= (int) data[len - 1];
		      }

		      h *= m;
		    }

		    h ^= h >>> 13;
		    h *= m;
		    h ^= h >>> 15;

		    return h%tablesize;
		  }



}
