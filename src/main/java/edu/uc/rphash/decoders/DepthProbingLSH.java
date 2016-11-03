package edu.uc.rphash.decoders;

import edu.uc.rphash.frequentItemSet.Countable;

public class DepthProbingLSH implements Decoder {
	Countable counter;
	Decoder decoder;
	
	public DepthProbingLSH(Countable counter, Decoder decoder){
		this.counter = counter;
		this.decoder = decoder;
	}

	@Override
	public int getDimensionality() {
		return decoder.getDimensionality();
	}

	public int countbits(long b){
		int i = 0 ;
		while(b!=0){
			b>>=1;
			i++;
		}
		return i;
	}
	

	
	/*
	 * foreseeable issues, cold start is broken
	 * 1<1.... since all counts start at 0
	 * 
	 * we may need alot of tuning
	 * 
	 * things that start with zeros will be misread as missing entropy
	 * 		how bad is this? 
	 * 		at least half are full bit width, and the probability of 
	 * 		two bits off is have of that, in essence it is exponentially
	 * 		fleating that our counts are off by alot for bit entropy
	 * (non-Javadoc)
	 * @see edu.uc.rphash.decoders.Decoder#decode(float[])
	 */
	
	@Override
	public long[] decode(float[] f) {
		long totalhash = 0;
		long previouscount = 0;
		boolean godeeper = true;
		while(godeeper){
			long curhash = decoder.decode(f)[0];
			totalhash^= (curhash);
			long currentcount = (long) counter.count(totalhash);
			godeeper = (previouscount >> countbits(curhash) < currentcount);
			previouscount = currentcount;
		}

		return new long[]{totalhash};
	}

	@Override
	public float getErrorRadius() {

		return -1;
	}

	@Override
	public float getDistance() {

		return 0;
	}

	@Override
	public boolean selfScaling() {

		return true;
	}

}
