package edu.uc.rphash;

import java.io.File;
import java.util.List;

import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

public class RPHash {

	static String[] rphashes = {"simple","3stage","mproj","mprobe","redux"};
	public static void main(String[] args) {
		
		
		if(args.length<3){
			System.out.println("Usage: rphash InputFile k OutputFile [simple(default),3stage,multiRP,multiProj,redux, kmeans, pkmeans]");
			System.exit(0);
		}
		
		List<float[]> data = TestUtil.readFile(new File(args[0]));
		int k =  Integer.parseInt(args[1]);
		String outputFile = args[2];
		if(args.length==3)
		{
			
			RPHashSimple clusterer = new RPHashSimple(data,k);
			TestUtil.writeFile(new File(outputFile),clusterer.getCentroids());
		}
		
		int i = 3;
		long startTime;
		while(i<args.length)
		{
				switch (args[i]) 
				{
		         	case "simple": 
		         	{
		        		
		         		System.out.print("Running Simple RPHash, processing time : ");
		         		
		         		Clusterer clusterer = new RPHashSimple(data,k);
		    			
		    			startTime = System.nanoTime();
		    			clusterer.getCentroids();
		        		System.out.println((System.nanoTime() - startTime)/1000000000f);
		        		
		    			TestUtil.writeFile(new File(outputFile+".smpl"),clusterer.getCentroids());
		    			break;
		         	}
		         	case "3stage": 
		         	{
		         		System.out.print("Running 3 Stage RPHash, processing time : ");
		         		Clusterer clusterer = new RPHash3Stage(data, k);
		    			
		    			startTime = System.nanoTime();
		    			clusterer.getCentroids();
		        		System.out.println((System.nanoTime() - startTime)/1000000000f);
		        		
		    			TestUtil.writeFile(new File(outputFile+".3stg"),clusterer.getCentroids());
		    			break;
		         	}
		         	case "multiRP": 
		         	{
		         		System.out.print("Running Multiple Run RPHash, processing time : ");
		         		Clusterer clusterer = new RPHashMultiRP(data, k);
		    			
		    			startTime = System.nanoTime();
		    			clusterer.getCentroids();
		        		System.out.println((System.nanoTime() - startTime)/1000000000f);
		        		
		    			TestUtil.writeFile(new File(outputFile+".multirp"),clusterer.getCentroids());
		    			break;
		         	}
		         	case "multiProj": 
		         	{
		         		System.out.print("Running Multi-Projection RPHash, processing time : ");
		         		Clusterer clusterer = new RPHashMultiProj(data, k);
		    			
		    			startTime = System.nanoTime();
		    			clusterer.getCentroids();
		        		System.out.println((System.nanoTime() - startTime)/1000000000f);
		        		
		    			TestUtil.writeFile(new File(outputFile+".mprp"),clusterer.getCentroids());
		    			break;
		         	}
		         	case "redux": 
		         	{
		         		System.out.print("Running Iterative Reduction RPHash, processing time : ");
		         		Clusterer clusterer = new RPHashIterativeRedux(data, k);
		    			
		    			startTime = System.nanoTime();
		    			clusterer.getCentroids();
		        		System.out.println((System.nanoTime() - startTime)/1000000000f);
		        		
		    			TestUtil.writeFile(new File(outputFile+".itrdx"),clusterer.getCentroids());
		    			break;
		         	}
		         	case "kmeans": 
		         	{
		         		System.out.print("Running Full Kmeans, processing time : ");
		         		Clusterer clusterer = new Kmeans(k, data);
		    			
		    			startTime = System.nanoTime();
		    			clusterer.getCentroids();
		        		System.out.println((System.nanoTime() - startTime)/1000000000f);
		        		
		    			TestUtil.writeFile(new File(outputFile+".kmn"),clusterer.getCentroids());
		    			break;
		         	}
		         	case "pkmeans": 
		         	{
		         		System.out.print("Running Projection Kmeans, processing time : ");
		         		Clusterer clusterer = new Kmeans(k, data,24);
		    			
		    			startTime = System.nanoTime();
		    			clusterer.getCentroids();
		        		System.out.println((System.nanoTime() - startTime)/1000000000f);
		        		
		    			TestUtil.writeFile(new File(outputFile+".pkmn"),clusterer.getCentroids());
		    			break;
		         	}
		         	default: {
		         		System.out.println(args[i]+" does not exist");
		         		break;
		         	}
		         	
				}
		         	
			i++;
			
		}
		
		
		
		
	}

}
