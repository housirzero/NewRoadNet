package kmeans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class MapSort<K, V> {
	
	public Map<K, V> map;
	
	public MapSort(Map<K, V> map) {
		this.map = map;
	}

	public List<MapClass<K, V>> sortByValue(){
		List<MapClass<K, V>> list = new ArrayList<MapClass<K, V>>();
		for(Entry<K, V> entry : map.entrySet())
			list.add(new MapClass<K, V>(entry.getKey(), entry.getValue()));
		Collections.sort(list, new SortByValue());
		return list;
	}

	public List<MapClass<K, V>> sortByKey(){
		List<MapClass<K, V>> list = new ArrayList<MapClass<K, V>>();
		for(Entry<K, V> entry : map.entrySet())
			list.add(new MapClass<K, V>(entry.getKey(), entry.getValue()));
		Collections.sort(list, new SortByKey());
		return list;
	}

	public abstract int valueComp(V v1, V v2);
	public abstract int keyComp(K k1, K k2);

	class SortByValue implements Comparator<MapClass<K, V>>
	{
		@Override
		public int compare(MapClass<K, V> o1, MapClass<K, V> o2) {
			return valueComp(o1.v, o2.v);
		}
	}
	
	class SortByKey implements Comparator<MapClass<K, V>>
	{
		@Override
		public int compare(MapClass<K, V> o1, MapClass<K, V> o2) {
			return keyComp(o1.k, o2.k);
		}
	}
}

