package uc.edu.rphash.Readers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class StreamObject implements RPHashObject {

	BufferedInputStream elements;
	int k;
	int n;
	int dim;
	int randomseed;
	int hashmod;
	long[] ids;
	
	String spacetoken(BufferedInputStream elements)throws IOException{
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
		return spacetoken(elements);
	}
	
	float getNextFloat()
	{
		try{
			return Float.parseFloat(spacetoken(elements));
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
				data[i] = Float.parseFloat(spacetoken(elements));
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

	// input format
	//per line
	//top ids list (integers)
	// --num of clusters ( == k)
	// --num of data( == n)
	// --num dimensions
	// --input random seed;
	StreamObject(BufferedInputStream elements)
	{
		try{
		k = Integer.parseInt(spacetoken(elements));
		n = Integer.parseInt(spacetoken(elements));
		dim = Integer.parseInt(spacetoken(elements));
		randomseed = Integer.parseInt(spacetoken(elements));
		hashmod = Integer.parseInt(spacetoken(elements));
	}catch(IOException e){
		System.err.println("Couldn't Read Datastream");
	}
	catch(NumberFormatException pe)
	{
		System.err.println("Couldn't Parse Stream Number Format Error ");	
	}
		
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
		this.ids = ids;
	}

	@Override
	public long[] getIDs() {	
		return ids;
	}
	
	public void setIDs(Set<Long> ids){
		this.ids = new long[ids.size()];
		Iterator<Long> it = ids.iterator();
		int i = 0;
		while(it.hasNext())
			this.ids[i++] = it.next();
	}
	
	@Override
	public void reset() {
		//current = 0;
	}
	
}

