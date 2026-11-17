import java.util.Comparator;

public interface MyNavigableMap<K, V> extends MyMap<K, V> {

    Comparator<? super K> comparator();

    K firstKey();

    K lastKey();

    Entry<K, V> firstEntry();

    Entry<K, V> lastEntry();

    // Наибольший ключ <= key
    K floorKey(K key);

    // Наибольший ключ < key
    K lowerKey(K key);

    // Наименьший ключ >= key
    K ceilingKey(K key);

    // Наименьший ключ > key
    K higherKey(K key);

    MyNavigableMap<K, V> subMap(K from, boolean fromInclusive, K to, boolean toInclusive);

    default MyNavigableMap<K, V> subMap(K from, K to) {
        return subMap(from, true, to, false);
    }

    MyNavigableMap<K, V> headMap(K to, boolean inclusive);

    MyNavigableMap<K, V> tailMap(K from, boolean inclusive);
}