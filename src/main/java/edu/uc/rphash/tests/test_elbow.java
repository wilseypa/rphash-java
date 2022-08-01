package edu.uc.rphash.tests;

import java.util.ArrayList;
//import java.util.Arrays;
import java.util.List;

import edu.uc.rphash.kneefinder.JythonTest;


public class test_elbow {
	

	public static void main(String[] args){
		

		List<Long> counts =  new ArrayList<>(50);
		
		double elbowdata[] = {5000,
				4000,
				3000,
				2000,
				1000,
				900,
				800,
				700,
				600,
				500,
				450,
				400,
				350,
				300,
				250,
				225,
				200,
				175,
				150,
				125,
				100,
				75,
				50,
				25,
				24,
				23,
				22,
				21,
				20,
				19,
				18,
				17,
				16,
				15,
				14,
				13,
				12,
				11,
				10,
				10,
				9,
				9,
				9,
				9,
				9,
				9,
				9,
				9,
				9,
				8,	
    } ;
		
		int size = elbowdata.length ;
		
	      for (int i= 0;i<size;i++)
	      {
	    	  Long element= (long) elbowdata[i];
	    	    	  
	    	  counts.add(    element   );
	
	      }
//	      System.out.print("\n" + " counts : " + counts);	
				
   JythonTest elbowcalculator = new JythonTest();
    
	// int cutoff_returned = 
   
			int num_of_clusters= elbowcalculator.find_elbow(counts);
			System.out.println("\n" + "No. of clusters = " +  num_of_clusters); 

}
}