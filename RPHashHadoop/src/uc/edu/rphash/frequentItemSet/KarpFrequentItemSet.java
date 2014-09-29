package uc.edu.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


public class KarpFrequentItemSet<E> implements ItemSet<E> {
	private static final long serialVersionUID = 4325962401818825137L;
	
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
	public HashMap<E,Integer> getTop() 
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

	public static void main(String[] args)
	{
		Random r = new Random();
		KarpFrequentItemSet<Integer> karp = new KarpFrequentItemSet<Integer>((float)(1./10000.0));
		SimpleFrequentItemSet<Integer> smpl = new SimpleFrequentItemSet<Integer>(20);
		int testsize = 10000000;
		int sets = 6;
		int numsetsperpartition = 10;
		int startval = 1000;
		
		/**START Make Test Data**/
		ArrayList<Integer> ar = new ArrayList<Integer>(testsize);
		for(int k = 0;k<sets;k++){
			for(int i =numsetsperpartition*k;i<numsetsperpartition*(k+1);i++)
			{
				for(int j = 0 ;j<startval;j++)ar.add(i);
			}
			startval/=2;
		}
		for(int i = ar.size();i<testsize;i++)ar.add(r.nextInt(testsize));
		Collections.shuffle(ar, r);
		/**END Make Test Data**/
		System.out.println("Begin Test");
		for(int i =0;i<testsize;i++)
		{
			karp.add(ar.get(i));
			smpl.add(ar.get(i));
		}
		HashMap<Integer,Integer> d = karp.getTop();
		HashMap<Integer,Integer> e = smpl.getTop();//takes roughly 5x longer
		for(Integer k: d.keySet())System.out.println(k+","+d.get(k));	
		for(Integer k: e.keySet())System.out.println(k+","+e.get(k));
	}
}
