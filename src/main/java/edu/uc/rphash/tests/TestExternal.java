package edu.uc.rphash.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.RPHashSimple;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.DepthProbingLSH;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

public class TestExternal {

	
	public static List<float[]> runExternalProc(String cmd,RPHashObject o) throws IOException, InterruptedException{
		
		VectorUtil.writeFile(new File("/tmp/ramdisk/rphash.mat"), o.getRawData(), false);
		
		final Process p = Runtime.getRuntime().exec(cmd + " /tmp/ramdisk/rphash.mat " + String.valueOf(o.getk()));
		new Thread(new Runnable() {
		    public void run() 
		    {
		     BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		     String line = null; 
		     
		     BufferedReader errorinput = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		     String errors = null; 
		     
		     try {
		        while ((line = input.readLine()) != null ||(errors = errorinput.readLine()) != null )
		        {
		            if(line!=null)System.out.println(line);
		            else System.out.println(errors);
		        }
		        
		     } catch (IOException e) {
		            e.printStackTrace();
		     }
		    }
		}).start();

		p.waitFor();
		
		return VectorUtil.readFile("/tmp/ramdisk/rphash.mat_output", false);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		

		int k = 10;
		int d = 1000;
		int n = 10000;
		float var = 0.1f;
		int count = 5;
		System.out.printf("Decoder: %s\n","Adaptive Python");
		System.out.printf("ClusterVar\t");
		for (int i = 0; i < count; i++)
			System.out.printf("Trial%d\t", i);
		System.out.printf("RealWCSS\n");
		
		for (float f = var; f < 3.01; f += .05f) {
			float avgrealwcss = 0;
			float avgtime = 0;
			System.out.printf("%f\t", f);
			for (int i = 0; i < count; i++) {
				GenerateData gen = new GenerateData(k, n / k, d, f, true, 1f);
				RPHashObject o = new SimpleArrayReader(gen.data, k);
				o.setDecoderType(new DepthProbingLSH(24));
				o.setDimparameter(24);
				
				long startTime = System.nanoTime();
				List<float[]> fltCentssr =  TestExternal.runExternalProc("pypy /home/lee/Desktop/reclsh/RunRecLSH.py",o);
				List<Centroid> centsr  = new ArrayList<>();
				for(float[] cnt : fltCentssr)centsr.add(new Centroid(cnt));
				avgtime += (System.nanoTime() - startTime) / 100000000;

				avgrealwcss += StatTests.WCSSEFloatCentroid(gen.getMedoids(),
						gen.getData());

				System.out.printf("%.0f\t",
						StatTests.WCSSECentroidsFloat(centsr, gen.data));
				System.gc();
				
			}
			System.out.printf("%.0f\n", avgrealwcss / count);
		}
	}

}
