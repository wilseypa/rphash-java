package edu.uc.rphash;

import java.io.File;
import java.util.List;

import edu.uc.rphash.tests.TestUtil;

public class RPHash {

	static String[] rphashes = {"simple","3stage","mproj","mprobe","redux"};
	public static void main(String[] args) {
		
		
		if(args.length<3){
			System.out.println("Usage: rphash InputFile k OutputFile [simple(default),3stage,multiRP,multiProj,redux]");
			System.exit(0);
		}
		
		List<float[]> data = TestUtil.readFile(new File(args[0]));
		int k =  Integer.parseInt(args[1]);
		String outputFile = args[2];
		if(args.length==3)
		{
			
			RPHashSimple rphit = new RPHashSimple(data,k);
			TestUtil.writeFile(new File(outputFile),rphit.getCentroids());
		}
		
		int i = 3;
		while(i<args.length)
		{
				switch (args[i]) 
				{
		         	case "simple": 
		         	{
		         		System.out.println("Running Simple RPHash");
		    			RPHashSimple rphit = new RPHashSimple(data,k);
		    			TestUtil.writeFile(new File(outputFile+".smpl"),rphit.getCentroids());
		    			break;
		         	}
		         	case "3stage": 
		         	{
		         		System.out.println("Running 3 Stage RPHash");
		    			RPHash3Stage rphit = new RPHash3Stage(data, k);
		    			TestUtil.writeFile(new File(outputFile+".3stg"),rphit.getCentroids());
		    			break;
		         	}
		         	case "multiRP": 
		         	{
		         		System.out.println("Running Multiple Run RPHash");
		    			RPHashMultiRP rphit = new RPHashMultiRP(data, k);
		    			TestUtil.writeFile(new File(outputFile+".multirp"),rphit.getCentroids());
		    			break;
		         	}
		         	case "multiProj": 
		         	{
		         		System.out.println("Running Multi-Projection RPHash");
		    			RPHashMultiProj rphit = new RPHashMultiProj(data, k);
		    			TestUtil.writeFile(new File(outputFile+".mprp"),rphit.getCentroids());
		    			break;
		         	}
		         	case "redux": 
		         	{
		         		System.out.println("Running Iterative Reduction RPHash");
		    			RPHashIterativeRedux rphit = new RPHashIterativeRedux(data, k);
		    			TestUtil.writeFile(new File(outputFile+".itrdx"),rphit.getCentroids());
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
