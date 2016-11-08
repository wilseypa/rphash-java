package edu.uc.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class KarpFrequentItemSet<E> implements ItemSet<E>{

	
	HashMap<E,Integer> data;
	int setsize;
	
	public KarpFrequentItemSet(float minFreq) 
	{
		this.setsize = (int)(1./minFreq);
		data = new HashMap<E, Integer>(setsize);
	}
	
	//from Karp's Frequent itemset counting this is the only 
	//method that needs to be changed
	@Override
	public boolean add(E e) {
		if(data.containsKey(e))
		{
			data.put(e,data.get(e)+1);
			return true;
		}
		
		if(data.size()<setsize)
		{
			data.put(e,1);
			return true;
		}
		//doesnt already contain key and the list is full
		//so we have to prune the list
		Iterator<E> it = data.keySet().iterator();
		//for(E key:data.keySet())
		int ct = 1;
		while(it.hasNext() /*&& ct != 0*/)
		{
			E n = it.next();
			ct = data.get(n)-1;
			data.put(n, ct);//overwrite
			if(ct==0){
				it.remove();
			}
		}
		//now if the list is less than maxsize put new object
		if(data.size()<setsize)
			data.put(e,1);
		return false;
	}

	@Override
	public ArrayList<E> getTop() 
	{	
		ArrayList<tuple<E>> sortedData = new ArrayList<tuple<E>>(data.size());
		for(E key:data.keySet())sortedData.add(new tuple<E>(key,data.get(key)));
		Collections.sort(sortedData);
		setsize = setsize<sortedData.size()?setsize:sortedData.size();
		
		ArrayList<E> ret = new ArrayList<E>(setsize);
		for(int i =0;i<setsize;i++){
			ret.add(i,sortedData.get(i).key);
		}
		return ret;
	}

	@Override
	public List<Long> getCounts() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object getBaseClass() {
		return null;
	}


}
