package com.csc108.model.cache;

import com.csc108.exceptions.CacheOperationException;
import com.csc108.model.ITimeable;
import com.csc108.model.data.Security;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*Single-thread cache, for multi-thread purpose, check MTimeSeresDataCache for reference*/
public class TimeSeriesDataCache<T extends ITimeable> extends AlgoCache<Security, List<T>> {
	@Getter
	private int sortCounter = 0;

	public void putAll(Security sec_, List<T> t_) {
		for (T b : t_) {
			put(sec_, b);
		}
	}

	public void put(Security sec_, T t_) {
		List<T> l = super.get(sec_);
		if (l == null) {
			l = Lists.<T>newArrayList();//new ArrayList<T>();
			put(sec_, l);
		}

		if (l.size() == 0) {
			l.add(t_);
			return;
		}

		// check if this entry is after the previous one
		T last = l.get(l.size() - 1);
		l.add(t_);
		if (t_.getDateTime().isAfter(last.getDateTime()) || t_.getDateTime().isEqual(last.getDateTime())) {

		} else {// make sure it is time-series
			sortCounter++;
			Collections.sort(l, ITimeable.TimeComparator);
		}
	}

	public List<T> getRange(Security sec_, T from_, T to_) {
		List<T> l = null;

		if (from_.getDateTime().isAfter(to_.getDateTime())) {
			throw new CacheOperationException(String.format("From[%s] is after to[%s]", from_, to_));
		}

		List<T> t = get(sec_);
		if(t == null) {
			return Lists.newArrayListWithExpectedSize(0);
		}

		int f1 = Collections.binarySearch(t, from_, ITimeable.TimeComparator2);
		int f2 = Collections.binarySearch(t, to_, ITimeable.TimeComparator3);
		int f = Collections.binarySearch(t, to_, ITimeable.TimeComparator);
		f1 = adjustIndexFrom(f1);
		f2 = adjustIndexTo(f2);

		if (f1 == f2 && f < 0) {
			l = new ArrayList<T>(0);
		} else {
			T tmp = t.get(0);
			@SuppressWarnings("unchecked")
			T[] obs = Arrays.copyOfRange(t.toArray((T[]) Array.newInstance(tmp.getClass(), 0)), f1, f2);
			l = Arrays.asList(obs);
		}

		return l;
	}

	@Override
	public List<T> get(Security sec_) {
		return super.get(sec_);
	}

	public T getLast(Security sec_) {
		List<T> cached = super.get(sec_);
		if (cached == null || cached.size() == 0)
			return null;
		else
			return cached.get(cached.size() - 1);
	}

	public int size(Security sec_) {
		List<T> l = super.get(sec_);
		return l == null ? 0 : l.size();
	}

	public void remove(Security sec_, T time_) {
		removeRange(sec_, time_, time_);
	}

	/**
	 * This function shouldn't be used frequently security level lock is ok
	 * 
	 * @param sec_
	 * @param from_
	 */
	@SuppressWarnings("unchecked")
	public void removeRange(Security sec_, T from_, T to_) {
		List<T> l = super.get(sec_);
		int f1 = Collections.binarySearch(l, from_, ITimeable.TimeComparator2);
		int f2 = Collections.binarySearch(l, to_, ITimeable.TimeComparator3);
		int f = Collections.binarySearch(l, to_, ITimeable.TimeComparator);
		f1 = adjustIndexFrom(f1);
		f2 = adjustIndexTo(f2);

		if (f1 == f2 && f < 0) {
			// nothing to remove
		} else {
			T t = l.get(0);
			T[] obs = l.toArray((T[]) Array.newInstance(t.getClass(), 0));
			T[] newObs = (T[]) Array.newInstance(obs.getClass().getComponentType(), f1 - 0 + obs.length - f2);
			if (f1 != 0) {
				System.arraycopy(obs, 0, newObs, 0, f1 - 0);
			}
			if (f2 != obs.length) {
				System.arraycopy(obs, f2, newObs, f1, obs.length - f2);
			}
			l = new ArrayList<T>(newObs.length);
			l.addAll(Arrays.asList(newObs));
			put(sec_, l);
		}
	}

	private int adjustIndexFrom(int from_) {
		return from_ >= 0 ? from_ : 0 - (from_ + 1);
	}

	private int adjustIndexTo(int to_) {
		return to_ >= 0 ? to_ : 0 - (to_ + 1);
	}

	public T getLastBeforeOrEqual(Security sec_, ITimeable t_) {
		T t = null;

		List<T> cached = super.get(sec_);
		if (cached == null || cached.size() == 0)
			return t;
		else {
			// use a for loop as the data is a time-series mode so the last should be hit by a high probability
			// for general purpose, a binary search should have better performance
			for (int i = cached.size() - 1; i >= 0; i--) {
				if (!cached.get(i).getDateTime().isAfter(t_.getDateTime())) {
					t = cached.get(i);
					break;
				}
			}
			return t;
		}
	}
}
