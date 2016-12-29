package edu.uc.rphash.frequentItemSet;

import java.util.List;


public interface ItemSet<E> extends Countable {
	class tuple<E> implements Comparable<tuple<E>>{
		E key;
		Integer value;

		public tuple(E key, Integer value) {
			this.key = key;
			this.value = value;
		}
		@Override
		public int compareTo(tuple<E> o) {
			return  o.value  -this.value;
		}
	}
	
	public Object getBaseClass();
	public List<E> getTop();
	public List<Long> getCounts();
	

}
