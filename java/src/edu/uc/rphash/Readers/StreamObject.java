package edu.uc.rphash.Readers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class StreamObject implements RPHashObject {

	BufferedInputStream elements;
	int k;
	int n;
	int dim;
	int randomseed;
	int hashmod;
	int times;
	List<float[]> centroids;
	List<Long> topIDs;

	
	// input format
	//per line
	//top ids list (integers)
	// --num of clusters ( == k)
	// --num of data( == n)
	// --num dimensions
	// --input random seed;
	StreamObject(BufferedInputStream elements)
	{

		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
		try{
			k = Integer.parseInt(spacetoken());
			n = Integer.parseInt(spacetoken());
			dim = Integer.parseInt(spacetoken());
			randomseed = Integer.parseInt(spacetoken());
			hashmod = Integer.parseInt(spacetoken());
			times = Integer.parseInt(spacetoken());
		}catch(IOException e){
			System.err.println("Couldn't Read Datastream");
		}
		catch(NumberFormatException pe)
		{
			System.err.println("Couldn't Parse Stream Number Format Error ");	
		}
		
	}
	

	String spacetoken()throws IOException{
		StringBuilder sb = new StringBuilder();
		char b = (char)elements.read();
		sb.append(elements.read());

		while(b!=' '){
			sb.append(b);
			b = (char)elements.read();
		}
		return sb.toString();
	}
	
	String getNext()throws IOException
	{
		return spacetoken();
	}
	
	float getNextFloat()
	{
		try{
			return Float.parseFloat(spacetoken());
		}
		catch(IOException e){
			System.err.println("Couldn't Read Datastream");
		}
		catch(NumberFormatException pe)
		{
			System.err.println("Couldn't Parse Stream Number Format Error ");	
		}
		return 0.0f;
	}

	public float[] getNextVector()
	{
		float[] data = new float[dim];
		int i = 0;
		try{
			while(i<dim)
				data[i++] = Float.parseFloat(spacetoken());
		}
		catch(IOException e){
			System.err.println("Couldn't Read Datastream");
		}
		catch(NumberFormatException pe)
		{
			System.err.println("Couldn't Parse Stream Number Format Error ");	
		}
		return data;
	}



	@Override
	public int getk() {
		return k;
	}

	@Override
	public int getn() {
		return n;
	}

	@Override
	public int getdim() {
		return dim;
	}
	
	@Override
	public int getHashmod(){
		return hashmod;
	}
	
	@Override
	public int getRandomSeed(){
		return randomseed;
	}




	
	@Override
	public void reset() {

			try{
				elements.reset();
			}catch(IOException ioe){
				ioe.printStackTrace();
			}
	}

	@Override
	public void addCentroid(float[] v) {
		centroids.add(v);
	}

	@Override
	public void setCentroids(List<float[]> l) {
		centroids = l;
	}


	@Override
	public int getTimes() {
		return times;
	}


	@Override
	public List<Long> getPreviousTopID() {

		return topIDs;
	}
	
	@Override
	public void setPreviousTopID(List<Long> top) 
	{
		topIDs = top;
	}


	@Override
	public Iterator<RPVector> getVectorIterator() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<float[]> getCentroids() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setRandomSeed(int seed){
		this.randomseed = seed;
	}

	
}

