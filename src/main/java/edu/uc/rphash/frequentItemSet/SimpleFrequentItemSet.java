package edu.uc.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.uc.rphash.util.VectorUtil;

public class SimpleFrequentItemSet<E> implements ItemSet<E> {

	int setsize;
	HashMap<E,Integer> data = new HashMap<E,Integer>();
	ArrayList<E> ret ;
	ArrayList<Long> counts ;
	public SimpleFrequentItemSet(int i) {
		this.setsize=i;
		ret=null ;
		counts=null ;

	}

	@Override
	public boolean add(Long e) {
		if(data.containsKey(e))
		{
			data.put((E)e,data.get(e)+1);
		}else
		{
			data.put((E)e,1);
		}
		return true;
		
	}
	
	
	
	public boolean add(E e) {
		if(data.containsKey(e))
		{
			data.put(e,data.get(e)+1);
		}else
		{
			data.put(e,1);
		}
		return true;
	}

	@Override
	public ArrayList<E> getTop() 
	{
		ArrayList<tuple<E>> sortedData = new ArrayList<tuple<E>>(data.size());

		for(E key:data.keySet())sortedData.add(new tuple<E>(key,data.get(key)));
		
		Collections.sort(sortedData);

		setsize = setsize<sortedData.size()?setsize:sortedData.size();
		
		ret = new ArrayList<E>(setsize);
		counts = new ArrayList<Long>(setsize);
		
		for(int i =0;i<setsize;i++){
			ret.add(i,sortedData.get(i).key);
			counts.add((long)sortedData.get(i).value);
		}
		return ret;
	}

	@Override
	public List<Long> getCounts() {
		if(counts!=null){
			getTop() ;
		}
		
		return counts;
	}

	@Override
	public Object getBaseClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float count(long item) {
		Integer ct = data.get(item);
		return ct==null?0:ct;
	}

}
