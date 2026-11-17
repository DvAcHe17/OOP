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

    public MyTreeMap() {
        this(null);
    }

    public MyTreeMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }

    @Override
    public V put(K key, V value) {
        Objects.requireNonNull(key, "Ключ не может быть null");

        Node<K, V> current = root;
        if (current == null) {
            // Сразу проверяем, что ключ можно сравнивать, даже если он первый
            compare(key, key);
            root = new Node<>(key, value, null);
            size = 1;
            return null;
        }

        int cmp;
        Node<K, V> parent;
        do {
            parent = current;
            cmp = compare(key, current.key);
            if (cmp < 0) {
                current = current.left;
            } else if (cmp > 0) {
                current = current.right;
            } else {
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
        } while (current != null);

        Node<K, V> newNode = new Node<>(key, value, parent);
        if (cmp < 0) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        fixAfterInsertion(newNode);
        size++;
        return null;
    }

    @Override
    public V get(K key) {
        Node<K, V> node = getNode(key);
        return node == null ? null : node.value;
    }

    @Override
    public V remove(K key) {
        Node<K, V> target = getNode(key);
        if (target == null) {
            return null;
        }
        V oldValue = target.value;
        deleteNode(target);
        return oldValue;
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

    // Вспомогательные методы для проверки балансировки в тестах

    public int height() {
        return calcHeight(root);
    }

    public boolean isValidRedBlackTree() {
        if (root == null) {
            return true;
        }
        if (root.color != BLACK || root.parent != null) {
            return false;
        }
        return validateSubtree(root, null, null) >= 0;
    }

    private Node<K, V> getNode(K key) {
        Objects.requireNonNull(key, "Ключ не может быть null");
        Node<K, V> current = root;
        while (current != null) {
            int cmp = compare(key, current.key);
            if (cmp < 0) {
                current = current.left;
            } else if (cmp > 0) {
                current = current.right;
            } else {
                return current;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private int compare(K a, K b) {
        if (comparator != null) {
            return comparator.compare(a, b);
        }
        return ((Comparable<? super K>) a).compareTo(b);
    }

    private Node<K, V> firstNode() {
        Node<K, V> node = root;
        if (node != null) {
            while (node.left != null) {
                node = node.left;
            }
        }
        return node;
    }

    private static <K, V> Node<K, V> successor(Node<K, V> node) {
        if (node == null) {
            return null;
        }
        if (node.right != null) {
            Node<K, V> p = node.right;
            while (p.left != null) {
                p = p.left;
            }
            return p;
        }
        Node<K, V> parent = node.parent;
        Node<K, V> child = node;
        while (parent != null && child == parent.right) {
            child = parent;
            parent = parent.parent;
        }
        return parent;
    }

    private static <K, V> boolean colorOf(Node<K, V> node) {
        return node == null ? BLACK : node.color;
    }

    private static <K, V> Node<K, V> parentOf(Node<K, V> node) {
        return node == null ? null : node.parent;
    }

    private static <K, V> Node<K, V> leftOf(Node<K, V> node) {
        return node == null ? null : node.left;
    }

    private static <K, V> Node<K, V> rightOf(Node<K, V> node) {
        return node == null ? null : node.right;
    }

    private static <K, V> void setColor(Node<K, V> node, boolean color) {
        if (node != null) {
            node.color = color;
        }
    }

    private void rotateLeft(Node<K, V> pivot) {
        if (pivot == null) {
            return;
        }
        Node<K, V> rightChild = pivot.right;
        pivot.right = rightChild.left;
        if (rightChild.left != null) {
            rightChild.left.parent = pivot;
        }
        rightChild.parent = pivot.parent;
        if (pivot.parent == null) {
            root = rightChild;
        } else if (pivot.parent.left == pivot) {
            pivot.parent.left = rightChild;
        } else {
            pivot.parent.right = rightChild;
        }
        rightChild.left = pivot;
        pivot.parent = rightChild;
    }

    private void rotateRight(Node<K, V> pivot) {
        if (pivot == null) {
            return;
        }
        Node<K, V> leftChild = pivot.left;
        pivot.left = leftChild.right;
        if (leftChild.right != null) {
            leftChild.right.parent = pivot;
        }
        leftChild.parent = pivot.parent;
        if (pivot.parent == null) {
            root = leftChild;
        } else if (pivot.parent.right == pivot) {
            pivot.parent.right = leftChild;
        } else {
            pivot.parent.left = leftChild;
        }
        leftChild.right = pivot;
        pivot.parent = leftChild;
    }

    private void fixAfterInsertion(Node<K, V> x) {
        x.color = RED;

        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                Node<K, V> uncle = rightOf(parentOf(parentOf(x)));
                if (colorOf(uncle) == RED) {
                    // Дядя красный: перекрашиваем отца и дядю в чёрный, деда — в красный
                    setColor(parentOf(x), BLACK);
                    setColor(uncle, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        // Зигзаг: выравниваем ветку малым поворотом
                        x = parentOf(x);
                        rotateLeft(x);
                    }
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

    private void deleteNode(Node<K, V> p) {
        size--;

        // Если у узла два потомка, копируем данные преемника и переключаемся на его удаление
        if (p.left != null && p.right != null) {
            Node<K, V> s = successor(p);
            p.key = s.key;
            p.value = s.value;
            p = s;
        }

        // Теперь у p не более одного потомка
        Node<K, V> replacement = (p.left != null) ? p.left : p.right;

        if (replacement != null) {
            replacement.parent = p.parent;
            if (p.parent == null) {
                root = replacement;
            } else if (p == p.parent.left) {
                p.parent.left = replacement;
            } else {
                p.parent.right = replacement;
            }

            p.left = null;
            p.right = null;
            p.parent = null;

            if (p.color == BLACK) {
                fixAfterDeletion(replacement);
            }
        } else if (p.parent == null) {
            // Удалили последний узел в дереве
            root = null;
        } else {
            // Удаляемый узел является листом; если он чёрный, сначала чиним баланс, потом отцепляем
            if (p.color == BLACK) {
                fixAfterDeletion(p);
            }

            if (p.parent != null) {
                if (p == p.parent.left) {
                    p.parent.left = null;
                } else if (p == p.parent.right) {
                    p.parent.right = null;
                }
                p.parent = null;
            }
        }
    }

    private void fixAfterDeletion(Node<K, V> x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                Node<K, V> sibling = rightOf(parentOf(x));

                // Случай 1: брат красный — делаем поворот, чтобы свести к случаю с чёрным братом
                if (colorOf(sibling) == RED) {
                    setColor(sibling, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sibling = rightOf(parentOf(x));
                }

                // Случай 2: оба ребёнка брата чёрные — отдаём чёрный цвет вверх
                if (colorOf(leftOf(sibling)) == BLACK && colorOf(rightOf(sibling)) == BLACK) {
                    setColor(sibling, RED);
                    x = parentOf(x);
                } else {
                    // Случай 3: дальний (правый) ребёнок брата чёрный — разворачиваем поддерево брата
                    if (colorOf(rightOf(sibling)) == BLACK) {
                        setColor(leftOf(sibling), BLACK);
                        setColor(sibling, RED);
                        rotateRight(sibling);
                        sibling = rightOf(parentOf(x));
                    }
                    // Случай 4: дальний ребёнок брата красный — окончательно выравниваем высоту
                    setColor(sibling, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sibling), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else {
                // Зеркальные случаи, когда x — правый потомок
                Node<K, V> sibling = leftOf(parentOf(x));

                if (colorOf(sibling) == RED) {
                    setColor(sibling, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sibling = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sibling)) == BLACK && colorOf(leftOf(sibling)) == BLACK) {
                    setColor(sibling, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sibling)) == BLACK) {
                        setColor(rightOf(sibling), BLACK);
                        setColor(sibling, RED);
                        rotateLeft(sibling);
                        sibling = leftOf(parentOf(x));
                    }
                    setColor(sibling, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sibling), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }

        setColor(x, BLACK);
    }

    private int calcHeight(Node<K, V> node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(calcHeight(node.left), calcHeight(node.right));
    }

    // Возвращает чёрную высоту поддерева или -1 при нарушении правил КЧ-дерева
    private int validateSubtree(Node<K, V> node, K min, K max) {
        if (node == null) {
            return 1;
        }
        if (min != null && compare(node.key, min) <= 0) {
            return -1;
        }
        if (max != null && compare(node.key, max) >= 0) {
            return -1;
        }
        if (node.color == RED && (colorOf(node.left) == RED || colorOf(node.right) == RED)) {
            return -1;
        }
        if ((node.left != null && node.left.parent != node)
                || (node.right != null && node.right.parent != node)) {
            return -1;
        }

        int leftBlackHeight = validateSubtree(node.left, min, node.key);
        int rightBlackHeight = validateSubtree(node.right, node.key, max);
        if (leftBlackHeight < 0 || rightBlackHeight < 0 || leftBlackHeight != rightBlackHeight) {
            return -1;
        }
        return leftBlackHeight + (node.color == BLACK ? 1 : 0);
    }

    private static final class Node<K, V> implements MyMap.Entry<K, V> {
        K key;
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