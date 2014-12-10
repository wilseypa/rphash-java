package edu.uc.rphash.tests;

import java.util.List;

public class StatTests {
	
	public static float PR(List<float[]> estCentroids, GenerateData gen){
		int count = 0 ;
		List<float[]> data = gen.data();
		for(int i = 0; i< data.size();i++)
		{
			if(TestUtil.findNearestDistance(data.get(i), estCentroids)==gen.getClusterID(i))count++;
		}
		return (float)count/(float)data.size();
	}
}
