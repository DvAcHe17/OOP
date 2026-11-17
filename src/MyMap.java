import java.util.List;

public interface MyMap<K, V> {
    interface Entry<K, V> {
        K getKey();

        V getValue();
    }
     //Связывает ключ со значением
     //@return прежнее значение для этого ключа или {@code null}, если ключа не было
     //@throws NullPointerException если ключ равен {@code null}
    V put(K key, V value);

    /** @return значение по ключу или {@code null}, если ключа нет */
    V get(K key);

    //Удаляет ключ из карты
    V remove(K key);

    boolean containsKey(K key);

    int size();

    boolean isEmpty();

    void clear();

    /** Снимок ключей в порядке обхода карты. */
    List<K> keys();

    /** Снимок значений в порядке обхода ключей. */
    List<V> values();
}