package edu.uc.rphash.standardhash;

public class FNVHash implements HashAlgorithm {

	
	int tablesize;
	public FNVHash(int tablesize){
		this.tablesize = tablesize;
	}
	
	@Override
	public long hash(long[] s) {
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
		return fnvHash(s2)%tablesize;
		
	}
	
	long fnvHash (byte[] bytes)
	{
		 //this hash key wont fit in a long needs the commented out 1
		 long hash = 216613626;//1;
		 for(byte key:bytes){
			 hash = (16777619 * hash) ^ key;
		 }
	      return hash ;
	}
	

}
