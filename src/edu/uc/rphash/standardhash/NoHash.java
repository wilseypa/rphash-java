package edu.uc.rphash.standardhash;

public class NoHash implements HashAlgorithm {

	long mod;
	
	public NoHash(long mod){
		this.mod = mod;
	}
	@Override
	public long hash(byte[] s) {
		long ret = s[0];
		for(int i = 1;i<s.length;i++){
			ret<<=8;
			ret+=s[i];
		}
		return ret%mod;
	}

}
