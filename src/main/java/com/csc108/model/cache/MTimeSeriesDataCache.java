package com.csc108.model.cache;

import com.csc108.exceptions.CacheOperationException;
import com.csc108.exceptions.TimeOutException;
import com.csc108.model.ITimeable;
import com.csc108.model.data.Security;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe timeseries data cache, for single-thread, pls use TimeSeriesDataCache
 * TODO: performance enhancement
 * 
 * Note: batch copy(Arrays.copy) is 10 times faster than for copy, 3 options as
 * below
 * 1. use lock segment to improve performance
 * 2. in-memory db quicker??? Derby : kdb
 */

public class MTimeSeriesDataCache<T extends ITimeable> extends AlgoCache<Security, List<T>> {
	//private final static int DEFAULT_LOCK_NUM = 100;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private Object lockObj = new Object();

	@Getter
	private int sortCounter = 0;

	private final static long DefaultTimeOut = 5000;

	public void putAll(Security sec_, List<T> t_) {
		for (T b : t_) {
			put(sec_, b);
		}
	}

	// security level lock is ok
	public void put(Security sec_, T t_) {
		boolean lockTaken = false;
		
		try {
			List<T> l = super.get(sec_);
			if (l == null) {
				synchronized (lockObj) {
					l = super.get(sec_);
					if (l == null) {
						l = Lists.<T>newArrayList();
						put(sec_, l);
					}
				}
			}

			lockTaken = lock.writeLock().tryLock(DefaultTimeOut, TimeUnit.MILLISECONDS);
			if(!lockTaken)
				throw new TimeOutException(String.format("Unable to acquire the lock after %s milliseconds", DefaultTimeOut));
			
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
		} catch (InterruptedException ex_) {			
			throw new CacheOperationException(ex_);
		} finally {
			if(lockTaken)
				lock.writeLock().unlock();
		}
	}

	/**
	 * security level lock is ok for performance purpose, the retained list is
	 * readonly
	 * 
	 * */
	public List<T> getRange(Security sec_, T from_, T to_) {
		List<T> l = null;
		boolean lockTaken = false;
		
		try {
			if (from_.getDateTime().isAfter(to_.getDateTime())) {
				throw new CacheOperationException(String.format("From[%s] is after to[%s]", from_, to_));
			}

			lockTaken = lock.readLock().tryLock(DefaultTimeOut, TimeUnit.MILLISECONDS);
			if(!lockTaken)
				throw new TimeOutException(String.format("Unable to acquire the lock after %s milliseconds", DefaultTimeOut));

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
				// block-copy is 10 times quicker than for copy				
				@SuppressWarnings("unchecked")				
				T[] obs = (T[]) Arrays.copyOfRange(t.toArray(new ITimeable[0]), f1, f2);
				l = Arrays.asList(obs);
			}
		} catch (InterruptedException ex_) {			
			throw new CacheOperationException(ex_);
		} finally {
			if(lockTaken)
				lock.readLock().unlock();
		}

		return l;
	}

	@Override
	public List<T> get(Security sec_) {
		List<T> l = new ArrayList<T>();
		List<T> cached = super.get(sec_);
		if (cached != null) {
			l.addAll(cached);
		}
		return l;
	}

	public T getLast(Security sec_) {
		boolean lockTaken = false;
		
		try {
			lockTaken = lock.readLock().tryLock(DefaultTimeOut, TimeUnit.MILLISECONDS);
			if(!lockTaken)
				throw new TimeOutException(String.format("Unable to acquire the lock after %s milliseconds", DefaultTimeOut));
			
			List<T> cached = super.get(sec_);
			if (cached == null || cached.size() == 0)
				return null;
			else
				return cached.get(cached.size() - 1);
		} catch (InterruptedException ex_) {			
			throw new CacheOperationException(ex_);
		} finally {
			if(lockTaken)
				lock.readLock().unlock();
		}
	}
	
	public T getLastBefore(Security sec_, ITimeable t_) {
		return null;
	}
	
	public T getFirstAfter(Security sec_, ITimeable t_) {
		return null;
	}
	
	public T getLastBeforeOrEqual(Security sec_, ITimeable t_) {
		boolean lockTaken = false;
		T t = null;
		try {
			lockTaken = lock.readLock().tryLock(DefaultTimeOut, TimeUnit.MILLISECONDS);
			if(!lockTaken)
				throw new TimeOutException(String.format("Unable to acquire the lock after %s milliseconds", DefaultTimeOut));
			
			List<T> cached = super.get(sec_);
			if (cached == null || cached.size() == 0)
				return t;
			else {
				//use a for loop as the data is a time-series mode so the last should be hit by a high probability
				//for general purpose, a binary search should have better performance
				for(int i = cached.size() - 1; i >= 0; i--) {
					if(!cached.get(i).getDateTime().isAfter(t_.getDateTime())) {
						t = cached.get(i);
						break;
					}
				}
				return t;
			}				
		} catch (InterruptedException ex_) {			
			throw new CacheOperationException(ex_);
		} finally {
			if(lockTaken)
				lock.readLock().unlock();
		}
	}
	
	public T getFirstAfterOrEqual(Security sec_, ITimeable t_) {
		return null;
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
		boolean lockTaken = false;

		try {
			lockTaken = lock.writeLock().tryLock(DefaultTimeOut, TimeUnit.MILLISECONDS);
			if(!lockTaken)
				throw new TimeOutException(String.format("Unable to acquire the lock after %s milliseconds", DefaultTimeOut));
						
			int f1 = Collections.binarySearch(l, from_, ITimeable.TimeComparator2);
			int f2 = Collections.binarySearch(l, to_, ITimeable.TimeComparator3);
			int f = Collections.binarySearch(l, to_, ITimeable.TimeComparator);
			f1 = adjustIndexFrom(f1);
			f2 = adjustIndexTo(f2);

			if (f1 == f2 && f < 0) {
				// nothing to remove
			} else {
				T[] obs = (T[]) l.toArray(new ITimeable[0]);
				/**
				 * OrderBook[] obs1 = f1 == 0 ? null : Arrays.copyOfRange(obs, 0, f1); 
				 * OrderBook[] obs2 = f2 == obs.length ? null : Arrays.copyOfRange(obs, f2, obs.length);
				 * 
				 * books = new ArrayList<OrderBook>();
				 * books.addAll(Arrays.asList(ArrayUtils.addAll(obs1, obs2)));
				 * put(sec_, books);
				 */
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
		} catch (InterruptedException ex_) {			
			throw new CacheOperationException(ex_);
		} finally {
			if(lockTaken)
				lock.writeLock().unlock();
		}
	}

	private int adjustIndexFrom(int from_) {
		return from_ >= 0 ? from_ : 0 - (from_ + 1);
	}

	private int adjustIndexTo(int to_) {
		return to_ >= 0 ? to_ : 0 - (to_ + 1);
	}
}
