package com.csc108.model.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

public abstract class AlgoCache<K, V> {
	/*CacheLoader<K, V> loader = new CacheLoader<K, V>() {
		public V load(K key) {
			return null;//TODO: use K to return a default
		}
	};*/
	Cache<K, V> cache = CacheBuilder.newBuilder().build();
	
	public long size() {		
		return cache.size();
	}
	
	public void put(K key_, V value_) {
		cache.put(key_, value_);
	}
	
	public V get(K key_) {
		return cache.getIfPresent(key_);
	}
	
	public void clear() {
		cache.invalidateAll();
	}
	
	public V remove(K key_) {
		V v = cache.getIfPresent(key_);
		cache.invalidate(key_);
		return v;
	}
	
	public boolean containsKey(K key_) {
		return cache.getIfPresent(key_) != null;
	}
	
	public Set<K> keySet() {
		return cache.asMap().keySet();
	}
	
	public Set<Entry<K, V>> entrySet() {
		return cache.asMap().entrySet();
	}
	
	public Collection<V> values() {
		return cache.asMap().values();
	}
}
