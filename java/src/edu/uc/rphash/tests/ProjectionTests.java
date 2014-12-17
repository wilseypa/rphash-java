package edu.uc.rphash.tests;

import java.util.Random;

import edu.uc.rphash.projections.FJLTProjection;
import edu.uc.rphash.projections.Projector;

public class ProjectionTests {
	static void testRPH(int n,int d,int multi){
		GenerateData gen = new GenerateData(n,2,d,.1f,false);
		Random rand= new Random();
		int nlog =multi;//(int)(Math.log(d) +.5)*multi;//round
		float sum =0.0f;
		float sumun = 0.0f;
		float sumreal =0.0f;
		float sumunreal = 0.0f;
		
		//Decoder dec = new LeechDecoder(1.3f);
		
		
		Projector[] p1 = new Projector[ nlog]; 
		for(int i =0;i<p1.length;i++)p1[i]=new /*Gaussian*/FJLTProjection(d,24,1);
		Projector[] p2 = new Projector[ nlog]; 
		for(int i =0;i<p2.length;i++)p2[i]=new /*Gaussian*/FJLTProjection(d,24,1);
		
		for(int i =0;i<gen.data.size();i+=2){
			//LSH lsh =  new LSH(dec,p1[0],hal);

			float[] f1 = TestUtil.avgProjection(gen.data.get(i), p1, 24);
			float[] f2 = TestUtil.avgProjection(gen.data.get(i+1), p2, 24);
			sum+=TestUtil.distance(f1,f2);
			sumreal +=TestUtil.distance(gen.data.get(i),gen.data.get(i+1));

			//get a random vector
			float[] r2 = gen.data.get(rand.nextInt(gen.data.size()));
			sumunreal +=TestUtil.distance(gen.data.get(i),r2);
			f2 = TestUtil.avgProjection(r2, p1, 24);
			sumun+=TestUtil.distance(f1,f2);
			
//			TestUtil.prettyPrint(f1);
//			TestUtil.prettyPrint(f2);
//			System.out.print(lsh.lshHash(f1)+":"+lsh.lshHash(f2));
//			System.out.println("----------------------------------"+sum+"--------------------------------------");
			
			
		}
		
		   System.out.println(multi+":"+sum/sumun+"|"+sumreal/sumunreal);
		
		
		
	}	
	public static void main(String[] args){
	/* for(int i = 0; i<100;i++)*/testRPH(1000,24,4);
	}
}
