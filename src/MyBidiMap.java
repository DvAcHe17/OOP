public interface MyBidiMap<K, V> extends MyMap<K, V> {

    K getKey(V value);

    boolean containsValue(V value);

    K removeByValue(V value);

    // Обратное представление (значение -> ключ), синхронизированное с основной картой
    MyBidiMap<V, K> inverse();
}