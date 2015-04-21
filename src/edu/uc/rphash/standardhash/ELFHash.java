package edu.uc.rphash.standardhash;

public class ELFHash implements HashAlgorithm {

	int tablesize;
	public ELFHash(int tablesize){
		this.tablesize = tablesize;
	}
	
	@Override
	public long hash(byte[] s) {
		// TODO Auto-generated method stub
		return elfHash(s) %tablesize;
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
