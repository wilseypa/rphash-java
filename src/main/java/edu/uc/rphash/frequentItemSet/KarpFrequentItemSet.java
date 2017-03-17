package edu.uc.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class KarpFrequentItemSet<E> implements ItemSet<Long>{

	
	HashMap<Long,Integer> data;
	int setsize;
	
	public KarpFrequentItemSet(float minFreq) 
	{
		this.setsize = (int)(1./minFreq);
		data = new HashMap<Long, Integer>(setsize);
	}
	
	//from Karp's Frequent itemset counting this is the only 
	//method that needs to be changed
	@Override
	public boolean add(Long e) {
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
		Iterator<Long> it = data.keySet().iterator();
		//for(E key:data.keySet())
		int ct = 1;
		while(it.hasNext() /*&& ct != 0*/)
		{
			Long n = it.next();
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
	public ArrayList<Long> getTop() 
	{	
		ArrayList<tuple<Long>> sortedData = new ArrayList<tuple<Long>>(data.size());
		for(Long key:data.keySet())sortedData.add(new tuple<Long>(key,data.get(key)));
		Collections.sort(sortedData);
		setsize = setsize<sortedData.size()?setsize:sortedData.size();
		
		ArrayList<Long> ret = new ArrayList<>(setsize);
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

	@Override
	public float count(long item) {
		Integer ct = data.get(item);
		return ct==null?0:ct;
	}


}
