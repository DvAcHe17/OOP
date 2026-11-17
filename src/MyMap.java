import java.util.List;

public interface MyMap<K, V> {

    interface Entry<K, V> {
        K getKey();
        V getValue();
    }

    V put(K key, V value);

    V get(K key);

    V remove(K key);

    boolean containsKey(K key);

    int size();

    boolean isEmpty();

    void clear();

    // Возвращает ключи в порядке обхода дерева
    List<K> keys();

    // Возвращает значения в порядке соответствующих им ключей
    List<V> values();
}