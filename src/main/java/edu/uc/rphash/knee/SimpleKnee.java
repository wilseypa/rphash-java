package edu.uc.rphash.knee;

public class SimpleKnee implements KneeAlgorithm {
	
	
	@Override
	public int findKnee(float[] data){
		if(data.length<2)return 1;
		float minval = (data[0]-data[1])/(data[0]);
		int minpt = 1;
		for(int i = 1;i<data.length;i++)
		{
			//some reasonable eps that will avoid asymptotic scaling errors
			float val = (data[i-1]-data[i])/(data[i-1]);
			if(val < minval)
			{
				minpt = i;
				minval = val;
			}
			
		}
		return minpt;
	}

}
