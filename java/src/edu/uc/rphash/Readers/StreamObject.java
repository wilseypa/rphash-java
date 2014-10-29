package edu.uc.rphash.Readers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class StreamObject implements RPHashObject {

	BufferedInputStream elements;
	int k;
	int n;
	int dim;
	int randomseed;
	int hashmod;
	List<Long> ids;
	List<Long> counts;
	List<float[]> centroids;
	int centit;
	int ctit;
	
	// input format
	//per line
	//top ids list (integers)
	// --num of clusters ( == k)
	// --num of data( == n)
	// --num dimensions
	// --input random seed;
	StreamObject(BufferedInputStream elements)
	{
		
		this.ids = new ArrayList<Long>();
		this.counts = new ArrayList<Long>();;
		this.centroids = new ArrayList<float[]>();
		this.centit = 0;
		this.ctit = 0;

		try{
			k = Integer.parseInt(spacetoken());
			n = Integer.parseInt(spacetoken());
			dim = Integer.parseInt(spacetoken());
			randomseed = Integer.parseInt(spacetoken());
			hashmod = Integer.parseInt(spacetoken());
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
	public void setIDs(long[] ids) {
		this.ids = new ArrayList<Long>(ids.length);
		for(int i=0;i<this.ids.size();i++)this.ids.add(ids[i]);
	}
	
	public void setIDs(List<Long> ids){
		this.ids = ids;
	}
	
	@Override
	public List<Long> getIDs() {	
		return ids;
	}

	@Override
	public void setCounts(long[] counts) {
		this.counts = new ArrayList<Long>(counts.length);
		for(int i=0;i<counts.length;i++)this.counts.add(counts[i]);
	}

	@Override
	public void setCounts(List<Long> ids) {
		this.ids = ids;
	}
	
	@Override
	public void reset() {
			centit = 0;
			ctit = 0;
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
	public List<float[]> getCentroids() {
		return centroids;
	}

	@Override
	public float[] getNextCentroid() {
		return centroids.get(centit++);
	}




	@Override
	public List<Long> getCounts() {
		// TODO Auto-generated method stub
		return null;
	}


//

	
}

