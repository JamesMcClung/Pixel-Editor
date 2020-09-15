package util;

public class Cache<K, V> {
	
	@FunctionalInterface
	public static interface Factory<K, V> {
		V get(K key);
	}

	public Cache(Factory<K,  V> factory) {
		this(null, null, factory);
	}
	
	public Cache(K initialKey, V initialVal, Factory<K, V> factory) {
		prevKey = initialKey;
		prevVal = initialVal;
		this.factory = factory;
	}
	
	private K prevKey;
	private V prevVal;
	private final Factory<K, V> factory;
	
	public V get(K key) {
		if (prevKey == null || !prevKey.equals(key)) {
			prevKey = key;
			prevVal = factory.get(key);
		}
		return prevVal;
	}
}
