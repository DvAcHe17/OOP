import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
public class MyTreeMap<K, V> implements MyMap<K, V> {

    private static final boolean RED = false;
    private static final boolean BLACK = true;

    private final Comparator<? super K> comparator;
    private Node<K, V> root;
    private int size;

    /** Карта с естественным порядком ключей (ключи должны быть Comparable). */
    public MyTreeMap() {
        this(null);
    }

    /** Карта с заданным порядком; {@code null} означает естественный порядок. */
    public MyTreeMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }

    // ---------------------------------------------------------------- API

    @Override
    public V put(K key, V value) {
        Objects.requireNonNull(key, "key must not be null");

        Node<K, V> t = root;
        if (t == null) {
            compare(key, key); // проверка типа/компаратора на первом ключе
            root = new Node<>(key, value, null);
            size = 1;
            return null;
        }

        int cmp;
        Node<K, V> parent;
        do {
            parent = t;
            cmp = compare(key, t.key);
            if (cmp < 0) {
                t = t.left;
            } else if (cmp > 0) {
                t = t.right;
            } else {
                V old = t.value;
                t.value = value;
                return old;
            }
        } while (t != null);

        Node<K, V> created = new Node<>(key, value, parent);
        if (cmp < 0) {
            parent.left = created;
        } else {
            parent.right = created;
        }
        fixAfterInsertion(created);
        size++;
        return null;
    }

    @Override
    public V get(K key) {
        Node<K, V> n = getNode(key);
        return n == null ? null : n.value;
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException("remove() будет реализован в День 2");
    }

    @Override
    public boolean containsKey(K key) {
        return getNode(key) != null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public List<K> keys() {
        List<K> result = new ArrayList<>(size);
        for (Node<K, V> n = firstNode(); n != null; n = successor(n)) {
            result.add(n.key);
        }
        return result;
    }

    @Override
    public List<V> values() {
        List<V> result = new ArrayList<>(size);
        for (Node<K, V> n = firstNode(); n != null; n = successor(n)) {
            result.add(n.value);
        }
        return result;
    }

    // ------------------------------------------------- диагностика (для тестов)

    /** Высота дерева (0 для пустого). */
    public int height() {
        return height(root);
    }

    /**
     * Проверяет все инварианты: порядок BST, чёрный корень,
     * отсутствие двух красных подряд, одинаковую чёрную высоту, корректность parent-ссылок.
     */
    public boolean isValidRedBlackTree() {
        if (root == null) {
            return true;
        }
        if (root.color != BLACK || root.parent != null) {
            return false;
        }
        return checkNode(root, null, null) >= 0;
    }

    // ------------------------------------------------------------ внутреннее

    private Node<K, V> getNode(K key) {
        Objects.requireNonNull(key, "key must not be null");
        Node<K, V> p = root;
        while (p != null) {
            int cmp = compare(key, p.key);
            if (cmp < 0) {
                p = p.left;
            } else if (cmp > 0) {
                p = p.right;
            } else {
                return p;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private int compare(K a, K b) {
        return comparator == null
                ? ((Comparable<? super K>) a).compareTo(b)
                : comparator.compare(a, b);
    }

    private Node<K, V> firstNode() {
        Node<K, V> p = root;
        if (p != null) {
            while (p.left != null) {
                p = p.left;
            }
        }
        return p;
    }

    private static <K, V> Node<K, V> successor(Node<K, V> t) {
        if (t == null) {
            return null;
        }
        if (t.right != null) {
            Node<K, V> p = t.right;
            while (p.left != null) {
                p = p.left;
            }
            return p;
        }
        Node<K, V> p = t.parent;
        Node<K, V> child = t;
        while (p != null && child == p.right) {
            child = p;
            p = p.parent;
        }
        return p;
    }

    private static <K, V> boolean colorOf(Node<K, V> p) {
        return p == null ? BLACK : p.color;
    }

    private static <K, V> Node<K, V> parentOf(Node<K, V> p) {
        return p == null ? null : p.parent;
    }

    private static <K, V> Node<K, V> leftOf(Node<K, V> p) {
        return p == null ? null : p.left;
    }

    private static <K, V> Node<K, V> rightOf(Node<K, V> p) {
        return p == null ? null : p.right;
    }

    private static <K, V> void setColor(Node<K, V> p, boolean color) {
        if (p != null) {
            p.color = color;
        }
    }

    private void rotateLeft(Node<K, V> p) {
        if (p == null) {
            return;
        }
        Node<K, V> r = p.right;
        p.right = r.left;
        if (r.left != null) {
            r.left.parent = p;
        }
        r.parent = p.parent;
        if (p.parent == null) {
            root = r;
        } else if (p.parent.left == p) {
            p.parent.left = r;
        } else {
            p.parent.right = r;
        }
        r.left = p;
        p.parent = r;
    }

    private void rotateRight(Node<K, V> p) {
        if (p == null) {
            return;
        }
        Node<K, V> l = p.left;
        p.left = l.right;
        if (l.right != null) {
            l.right.parent = p;
        }
        l.parent = p.parent;
        if (p.parent == null) {
            root = l;
        } else if (p.parent.right == p) {
            p.parent.right = l;
        } else {
            p.parent.left = l;
        }
        l.right = p;
        p.parent = l;
    }

    /** Восстанавливает инварианты после вставки красного узла {@code x}. */
    private void fixAfterInsertion(Node<K, V> x) {
        x.color = RED;

        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                Node<K, V> uncle = rightOf(parentOf(parentOf(x)));
                if (colorOf(uncle) == RED) {
                    // Случай 1: дядя красный, перекрашиваем и поднимаемся выше
                    setColor(parentOf(x), BLACK);
                    setColor(uncle, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        // Случай 2: «колено», сводим к случаю 3
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    // Случай 3: поворот деда
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {
                Node<K, V> uncle = leftOf(parentOf(parentOf(x)));
                if (colorOf(uncle) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(uncle, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = BLACK;
    }

    private int height(Node<K, V> n) {
        return n == null ? 0 : 1 + Math.max(height(n.left), height(n.right));
    }

    /**
     * @return чёрная высота поддерева или -1, если нарушен какой-либо инвариант.
     *         Ключи поддерева должны лежать строго в интервале (min, max).
     */
    private int checkNode(Node<K, V> n, K min, K max) {
        if (n == null) {
            return 1;
        }
        if (min != null && compare(n.key, min) <= 0) {
            return -1;
        }
        if (max != null && compare(n.key, max) >= 0) {
            return -1;
        }
        if (n.color == RED && (colorOf(n.left) == RED || colorOf(n.right) == RED)) {
            return -1;
        }
        if ((n.left != null && n.left.parent != n) || (n.right != null && n.right.parent != n)) {
            return -1;
        }
        int leftBlack = checkNode(n.left, min, n.key);
        int rightBlack = checkNode(n.right, n.key, max);
        if (leftBlack < 0 || rightBlack < 0 || leftBlack != rightBlack) {
            return -1;
        }
        return leftBlack + (n.color == BLACK ? 1 : 0);
    }

    /** Узел дерева. Новый узел создаётся чёрным, красным его делает fixAfterInsertion. */
    private static final class Node<K, V> implements MyMap.Entry<K, V> {
        final K key;
        V value;
        Node<K, V> left;
        Node<K, V> right;
        Node<K, V> parent;
        boolean color = BLACK;

        Node(K key, V value, Node<K, V> parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }
    }
}