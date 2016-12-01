package edu.uc.rphash.standardhash;

public class ELFHash implements HashAlgorithm {

	int tablesize;
	public ELFHash(int tablesize){
		this.tablesize = tablesize;
	}
	
	@Override
	public long hash(long d) {
		// TODO Auto-generated method stub
		byte[] s2 = new byte[8];
		int ct = 0;
	
		s2[ct++] = (byte)(d >>> 56);
		s2[ct++] = (byte)(d >>> 48);
		s2[ct++] = (byte)(d >>> 40);
		s2[ct++] = (byte)(d >>> 32);
		s2[ct++] = (byte)(d >>> 24);
		s2[ct++] = (byte)(d >>> 16);
		s2[ct++] = (byte)(d >>> 8 );
		s2[ct++] = (byte)(d       );
		
		return elfHash(s2) %tablesize;
	}
	
	@Override
	public long hash(long[] s) {
		// TODO Auto-generated method stub
		byte[] s2 = new byte[s.length*8];
		int ct = 0;
		for (long d:s) {
			s2[ct++] = (byte)(d >>> 56);
			s2[ct++] = (byte)(d >>> 48);
			s2[ct++] = (byte)(d >>> 40);
			s2[ct++] = (byte)(d >>> 32);
			s2[ct++] = (byte)(d >>> 24);
			s2[ct++] = (byte)(d >>> 16);
			s2[ct++] = (byte)(d >>> 8 );
			s2[ct++] = (byte)(d       );
		}
		return elfHash(s2) %tablesize;
	}
	
	//unsigned long ELFHash(const unsigned char *key,int tablesize)
	long elfHash(byte[]hash)
	{
	  long h = 0;
	  for(byte key : hash){
	    h = (h << 4) + (key&0xFF);
	    long g = h & 0xF0000000L;
	    if (g!=0) h ^= g >> 24;
	    h &= ~g;
	  }
	  return h;
	}

}
