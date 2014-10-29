package edu.uc.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.streaminer.stream.frequency.StickySampling;
import org.streaminer.stream.frequency.util.CountEntry;

public class StickyWrapper<E> implements ItemSet<E> {
	int k;
	StickySampling<E> scounter;
	ArrayList<E> topItems;
	ArrayList<Long> topCounts;
	
	public StickyWrapper(int k, int n){
		this.k=k;
		topItems = null;
		topCounts = null;
		scounter = null;
		scounter = new StickySampling<E>((float)k/((float)n*10),.0001,.0001);
	}
	@Override
	public boolean add(E e) {
		scounter.add(e, 1);
		return false;
	}

	private void populateLists(){
		
		topCounts = new ArrayList<Long>(k);
		topItems = new ArrayList<E>(k);
		
		List<CountEntry<E>> topcountentries = scounter.peek(k);
		Iterator<CountEntry<E>> it = topcountentries.iterator();
		while(it.hasNext()){
			CountEntry<E> tmp = it.next();
			topItems.add(tmp.item);
			topCounts.add(tmp.frequency);
		}	
	}
	
	@Override
	public List<E> getTop() {
		if(topItems == null)populateLists();
		return topItems;
	}
	@Override
	public List<Long> getCounts() {
		if(topItems == null)populateLists();
		return topCounts;
	}

}
