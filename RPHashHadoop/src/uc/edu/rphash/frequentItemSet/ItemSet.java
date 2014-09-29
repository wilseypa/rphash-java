package uc.edu.rphash.frequentItemSet;

import java.util.HashMap;


public interface ItemSet<E> {
	class tuple<E> implements Comparable<tuple>{
		E key;
		Integer value;

		public tuple(E key, Integer value) {
			this.key = key;
			this.value = value;
		}
		@Override
		public int compareTo(tuple o) {
			return  o.value  -this.value;
		}
	}
	public boolean add(E e);
	public HashMap<E,Integer> getTop();

}
