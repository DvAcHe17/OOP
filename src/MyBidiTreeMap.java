import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MyBidiTreeMap<K, V> implements MyBidiMap<K, V> {

    private final MyTreeMap<K, V> forward;
    private final MyTreeMap<V, K> backward;
    private MyBidiMap<V, K> inverseView;

    public MyBidiTreeMap() {
        this(null, null);
    }

    public MyBidiTreeMap(Comparator<? super K> keyComparator, Comparator<? super V> valueComparator) {
        this.forward = new MyTreeMap<>(keyComparator);
        this.backward = new MyTreeMap<>(valueComparator);
    }

    // Внутренний конструктор для создания обратного представления поверх тех же деревьев
    private MyBidiTreeMap(MyTreeMap<K, V> forward, MyTreeMap<V, K> backward, MyBidiMap<V, K> inverseView) {
        this.forward = forward;
        this.backward = backward;
        this.inverseView = inverseView;
    }

    @Override
    public V put(K key, V value) {
        Objects.requireNonNull(key, "Ключ не может быть null");
        Objects.requireNonNull(value, "Значение в BidiMap не может быть null");

        V oldValue = forward.put(key, value);
        if (oldValue != null) {
            backward.remove(oldValue);
        }

        try {
            K oldKey = backward.put(value, key);
            if (oldKey != null) {
                forward.remove(oldKey);
            }
        } catch (RuntimeException e) {
            // Откатываем изменение в forward, если value оказалось несравнимым
            if (oldValue != null) {
                forward.put(key, oldValue);
            } else {
                forward.remove(key);
            }
            throw e;
        }

        return oldValue;
    }

    @Override
    public V get(K key) {
        return forward.get(key);
    }

    @Override
    public K getKey(V value) {
        return backward.get(value);
    }

    @Override
    public V remove(K key) {
        V removedValue = forward.remove(key);
        if (removedValue != null) {
            backward.remove(removedValue);
        }
        return removedValue;
    }

    @Override
    public K removeByValue(V value) {
        K removedKey = backward.remove(value);
        if (removedKey != null) {
            forward.remove(removedKey);
        }
        return removedKey;
    }

    @Override
    public boolean containsKey(K key) {
        return forward.containsKey(key);
    }

    @Override
    public boolean containsValue(V value) {
        return backward.containsKey(value);
    }

    @Override
    public int size() {
        return forward.size();
    }

    @Override
    public boolean isEmpty() {
        return forward.isEmpty();
    }

    @Override
    public void clear() {
        forward.clear();
        backward.clear();
    }

    @Override
    public List<K> keys() {
        return forward.keys();
    }

    @Override
    public List<V> values() {
        return forward.values();
    }

    @Override
    public MyBidiMap<V, K> inverse() {
        if (inverseView == null) {
            inverseView = new MyBidiTreeMap<>(backward, forward, this);
        }
        return inverseView;
    }

    // Диагностический метод для тестов: проверяет синхронность обоих деревьев
    public boolean isConsistent() {
        if (forward.size() != backward.size()) {
            return false;
        }
        if (!forward.isValidRedBlackTree() || !backward.isValidRedBlackTree()) {
            return false;
        }
        for (K key : forward.keys()) {
            V value = forward.get(key);
            if (value == null || !Objects.equals(backward.get(value), key)) {
                return false;
            }
        }
        return true;
    }
}