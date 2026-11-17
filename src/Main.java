import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

public class Main {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("День 1: MyTreeMap — вставка и балансировка\n");

        testEmptyMap();
        testPutGetOverwrite();
        testNullKey();
        testAscendingInsertion();
        testDescendingInsertion();
        testCustomComparator();
        testStringKeys();
        testNullValueAllowed();
        testClear();
        testRandomAgainstJdk();
        testRemoveNotYetImplemented();

        System.out.println("\nИтого: прошло " + passed + ", упало " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testEmptyMap() {
        MyTreeMap<Integer, String> map = new MyTreeMap<>();
        check("пустая карта: size == 0", map.size() == 0);
        check("пустая карта: isEmpty()", map.isEmpty());
        check("пустая карта: get -> null", map.get(1) == null);
        check("пустая карта: containsKey -> false", !map.containsKey(1));
        check("пустая карта: инварианты", map.isValidRedBlackTree());
        check("пустая карта: height == 0", map.height() == 0);
    }

    private static void testPutGetOverwrite() {
        MyTreeMap<Integer, String> map = new MyTreeMap<>();
        check("put нового ключа возвращает null", map.put(10, "ten") == null);
        map.put(5, "five");
        map.put(15, "fifteen");
        check("size == 3", map.size() == 3);
        check("get(5)", "five".equals(map.get(5)));
        check("put существующего ключа возвращает старое значение", "ten".equals(map.put(10, "TEN")));
        check("значение перезаписано", "TEN".equals(map.get(10)));
        check("перезапись не меняет size", map.size() == 3);
        check("keys() отсортированы", map.keys().equals(List.of(5, 10, 15)));
        check("values() в порядке ключей", map.values().equals(List.of("five", "TEN", "fifteen")));
        check("инварианты", map.isValidRedBlackTree());
    }

    private static void testNullKey() {
        MyTreeMap<String, Integer> map = new MyTreeMap<>();
        boolean putThrows = false;
        boolean getThrows = false;
        try {
            map.put(null, 1);
        } catch (NullPointerException e) {
            putThrows = true;
        }
        try {
            map.get(null);
        } catch (NullPointerException e) {
            getThrows = true;
        }
        check("put(null) -> NullPointerException", putThrows);
        check("get(null) -> NullPointerException", getThrows);
    }

    private static void testAscendingInsertion() {
        // Худший случай для обычного BST, RB-дерево должно балансироваться
        MyTreeMap<Integer, Integer> map = new MyTreeMap<>();
        int n = 1000;
        for (int i = 1; i <= n; i++) {
            map.put(i, i * i);
        }
        check("возрастающая вставка: size == 1000", map.size() == n);
        check("возрастающая вставка: инварианты", map.isValidRedBlackTree());
        check("возрастающая вставка: height <= 2*log2(n+1) (height=" + map.height() + ")",
                map.height() <= maxHeight(n));
        check("возрастающая вставка: ключи отсортированы", isSortedAscending(map.keys()));
    }

    private static void testDescendingInsertion() {
        MyTreeMap<Integer, Integer> map = new MyTreeMap<>();
        int n = 1000;
        for (int i = n; i >= 1; i--) {
            map.put(i, i);
        }
        check("убывающая вставка: инварианты", map.isValidRedBlackTree());
        check("убывающая вставка: height <= 2*log2(n+1) (height=" + map.height() + ")",
                map.height() <= maxHeight(n));
        check("убывающая вставка: ключи отсортированы", isSortedAscending(map.keys()));
    }

    private static void testCustomComparator() {
        MyTreeMap<Integer, String> map = new MyTreeMap<>(Comparator.reverseOrder());
        map.put(1, "a");
        map.put(3, "c");
        map.put(2, "b");
        check("свой компаратор: обратный порядок", map.keys().equals(List.of(3, 2, 1)));
        check("свой компаратор: инварианты", map.isValidRedBlackTree());
    }

    private static void testStringKeys() {
        MyTreeMap<String, Integer> map = new MyTreeMap<>();
        for (String s : new String[]{"pear", "apple", "mango", "banana", "cherry"}) {
            map.put(s, s.length());
        }
        check("строковые ключи отсортированы",
                map.keys().equals(List.of("apple", "banana", "cherry", "mango", "pear")));
    }

    private static void testNullValueAllowed() {
        MyTreeMap<Integer, String> map = new MyTreeMap<>();
        map.put(1, null);
        check("null-значение допустимо: containsKey == true", map.containsKey(1));
        check("null-значение допустимо: get == null", map.get(1) == null);
        check("null-значение допустимо: size == 1", map.size() == 1);
    }

    private static void testClear() {
        MyTreeMap<Integer, Integer> map = new MyTreeMap<>();
        for (int i = 0; i < 50; i++) {
            map.put(i, i);
        }
        map.clear();
        check("clear: size == 0", map.size() == 0 && map.isEmpty());
        check("clear: ключей нет", map.keys().isEmpty());
        map.put(7, 7);
        check("после clear карта работает", map.size() == 1 && map.get(7) == 7);
    }

    private static void testRandomAgainstJdk() {
        Random rnd = new Random(42);
        MyTreeMap<Integer, Integer> mine = new MyTreeMap<>();
        TreeMap<Integer, Integer> jdk = new TreeMap<>();
        boolean sameReturns = true;
        boolean alwaysValid = true;

        for (int i = 0; i < 20_000; i++) {
            int key = rnd.nextInt(5000);
            int value = rnd.nextInt();
            Integer a = mine.put(key, value);
            Integer b = jdk.put(key, value);
            if (a == null ? b != null : !a.equals(b)) {
                sameReturns = false;
            }
            if (i % 500 == 0 && !mine.isValidRedBlackTree()) {
                alwaysValid = false;
            }
        }
        check("стресс-тест: put возвращает то же, что java.util.TreeMap", sameReturns);
        check("стресс-тест: инварианты в процессе", alwaysValid);
        check("стресс-тест: инварианты в конце", mine.isValidRedBlackTree());
        check("стресс-тест: size совпадает", mine.size() == jdk.size());
        check("стресс-тест: keys совпадают", mine.keys().equals(new ArrayList<>(jdk.keySet())));
        check("стресс-тест: values совпадают", mine.values().equals(new ArrayList<>(jdk.values())));

        boolean getsMatch = true;
        for (int k = -10; k < 5010; k++) {
            Integer x = mine.get(k);
            Integer y = jdk.get(k);
            if (x == null ? y != null : !x.equals(y)) {
                getsMatch = false;
                break;
            }
        }
        check("стресс-тест: get совпадает для всех ключей", getsMatch);
    }

    private static void testRemoveNotYetImplemented() {
        MyTreeMap<Integer, Integer> map = new MyTreeMap<>();
        map.put(1, 1);
        boolean thrown = false;
        try {
            map.remove(1);
        } catch (UnsupportedOperationException e) {
            thrown = true;
        }
        check("remove пока не реализован (будет в День 2)", thrown);
    }

    // ------------------------------------------------------------- helpers

    private static int maxHeight(int n) {
        return (int) Math.floor(2 * (Math.log(n + 1) / Math.log(2)));
    }

    private static boolean isSortedAscending(List<Integer> list) {
        List<Integer> copy = new ArrayList<>(list);
        Collections.sort(copy);
        return copy.equals(list);
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [OK]   " + name);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name);
        }
    }
}