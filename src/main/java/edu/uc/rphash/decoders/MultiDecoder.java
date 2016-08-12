package edu.uc.rphash.decoders;




public class MultiDecoder implements Decoder {
	
	Decoder innerDec;
	int dimension;
	int rounds;
	float distance = -1f;
	public MultiDecoder(int dimension , Decoder innerDec)
	{	
		this.innerDec = innerDec;
		rounds = (int) Math.ceil((double)dimension/(double)innerDec.getDimensionality());
		this.dimension = dimension;
		
	}
	
	public Decoder getInnerDecoder(){
		return innerDec;
	}
	
	@Override
	public int getDimensionality() {

		return dimension;
	}

	@Override
	public long[] decode(float[] f) 
	{
		if(innerDec.getDimensionality() == f.length){
			return innerDec.decode(f);//no looping needed
		}
		
		float[] innerpartition = new float[innerDec.getDimensionality()];
		int numRounds = (int) Math.ceil((double)dimension/(double)innerDec.getDimensionality());
		System.arraycopy(f, 0, innerpartition, 0, Math.min(f.length, innerpartition.length));
		long[] tmp = innerDec.decode(innerpartition);
		
		int retLength = tmp.length;
		
		long[] ret = new long[retLength*numRounds];
		System.arraycopy(tmp, 0, ret, 0, retLength);
		this.distance = innerDec.getDistance();
		for(int i = 1;i<numRounds;i++)
		{
			System.arraycopy(f, i*innerDec.getDimensionality(), innerpartition, 0, Math.min(f.length-i*innerDec.getDimensionality(), innerpartition.length));
			tmp = innerDec.decode(innerpartition);
			this.distance+=innerDec.getDistance();
			System.arraycopy(tmp, 0, ret, i*retLength, retLength);
		}
		return  ret;
	}

	@Override
	public float getErrorRadius() {
		return innerDec.getErrorRadius();
	}
	@Override
	public float getDistance() {
		return this.distance;
	}
	@Override
	public void setVariance(Float parameterObject) {
		innerDec.setVariance(parameterObject);
	}
	@Override
	public boolean selfScaling() {
		return innerDec.selfScaling();
	}
	@Override
	public float getVariance(){
		return innerDec.getVariance();
	}

}
