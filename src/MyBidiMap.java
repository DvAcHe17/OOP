public interface MyBidiMap<K, V> extends MyMap<K, V> {

    /** @return ключ, связанный со значением, или {@code null}, если такого нет */
    K getKey(V value);

    boolean containsValue(V value);

    /**
     * Удаляет связь по значению.
     *
     * @return удалённый ключ или {@code null}, если значения не было
     */
    K removeByValue(V value);

    /** Обратная карта (значение → ключ), работающая поверх тех же данных. */
    MyBidiMap<V, K> inverse();
}