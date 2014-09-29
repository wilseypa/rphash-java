package uc.edu.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import uc.edu.rphash.frequentItemSet.ItemSet.tuple;

public class SimpleFrequentItemSet<E> implements ItemSet<E> {

	int setsize;
	HashMap<E,Integer> data = new HashMap<E,Integer>();
	public SimpleFrequentItemSet(int i) {
		this.setsize=i;
	}

	@Override
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
	public HashMap<E, Integer> getTop() 
	{
		ArrayList<tuple<E>> sortedData = new ArrayList<tuple<E>>(data.size());
		for(E key:data.keySet())sortedData.add(new tuple<E>(key,data.get(key)));
		Collections.sort(sortedData);
		setsize = setsize<sortedData.size()?setsize:sortedData.size();
		
		
		HashMap<E,Integer> ret = new HashMap<E,Integer>(setsize);
		for(int i =0;i<setsize;i++){
			ret.put(sortedData.get(i).key, sortedData.get(i).value);
		}
		return ret;
	}

}
