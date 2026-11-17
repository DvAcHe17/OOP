import java.util.Comparator;
public interface MyNavigableMap<K, V> extends MyMap<K, V> {

    /** @return компаратор или {@code null}, если используется естественный порядок */
    Comparator<? super K> comparator();

    /** @throws java.util.NoSuchElementException если карта пуста */
    K firstKey();

    /** @throws java.util.NoSuchElementException если карта пуста */
    K lastKey();

    /** @return наименьшая запись или {@code null}, если карта пуста */
    Entry<K, V> firstEntry();

    /** @return наибольшая запись или {@code null}, если карта пуста */
    Entry<K, V> lastEntry();

    /** Наибольший ключ, меньший или равный {@code key}; {@code null}, если такого нет. */
    K floorKey(K key);

    /** Наибольший ключ, строго меньший {@code key}; {@code null}, если такого нет. */
    K lowerKey(K key);

    /** Наименьший ключ, больший или равный {@code key}; {@code null}, если такого нет. */
    K ceilingKey(K key);

    /** Наименьший ключ, строго больший {@code key}; {@code null}, если такого нет. */
    K higherKey(K key);

    /**
     * Подкарта с ключами из диапазона. Возвращается независимый снимок (копия).
     *
     * @throws IllegalArgumentException если {@code from > to}
     */
    MyNavigableMap<K, V> subMap(K from, boolean fromInclusive, K to, boolean toInclusive);

    /** Подкарта [from, to). */
    default MyNavigableMap<K, V> subMap(K from, K to) {
        return subMap(from, true, to, false);
    }

    /** Все ключи меньше {@code to} (или равные, если {@code inclusive}). */
    MyNavigableMap<K, V> headMap(K to, boolean inclusive);

    /** Все ключи больше {@code from} (или равные, если {@code inclusive}). */
    MyNavigableMap<K, V> tailMap(K from, boolean inclusive);
}