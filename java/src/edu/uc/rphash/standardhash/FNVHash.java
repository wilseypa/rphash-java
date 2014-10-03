package edu.uc.rphash.standardhash;

public class FNVHash implements HashAlgorithm {

	
	int tablesize;
	public FNVHash(int tablesize){
		this.tablesize = tablesize;
	}
	
	@Override
	public long hash(byte[] s) {
		
		return fnvHash(s)%tablesize;
		
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
