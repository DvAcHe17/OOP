import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

public class Main {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== День 2: MyTreeMap — удаление и ребалансировка ===\n");

        testEmptyMap();
        testPutGetAndOverwrite();
        testNullHandling();
        testSortedInsertions();
        testCustomComparator();
        testClear();
        testRemoveEdgeCases();
        testRemoveStructuralCases();
        testSequentialDeletions();
        testRandomPutAndRemoveAgainstJdk();

        System.out.println("\nРезультат: пройдено " + passed + ", провалено " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testEmptyMap() {
        MyTreeMap<Integer, String> map = new MyTreeMap<>();
        check("пустая карта: size == 0", map.size() == 0);
        check("пустая карта: isEmpty() == true", map.isEmpty());
        check("пустая карта: get возвращает null", map.get(42) == null);
        check("пустая карта: containsKey == false", !map.containsKey(42));
        check("пустая карта: инварианты КЧ-дерева", map.isValidRedBlackTree());
    }

    private static void testPutGetAndOverwrite() {
        MyTreeMap<Integer, String> map = new MyTreeMap<>();
        check("вставка нового ключа возвращает null", map.put(10, "ten") == null);
        map.put(5, "five");
        map.put(15, "fifteen");

        check("size == 3", map.size() == 3);
        check("get(5) == five", "five".equals(map.get(5)));
        check("перезапись возвращает старое значение", "ten".equals(map.put(10, "TEN")));
        check("новое значение сохранилось", "TEN".equals(map.get(10)));
        check("размер после перезаписи не вырос", map.size() == 3);
        check("ключи отсортированы", map.keys().equals(List.of(5, 10, 15)));
        check("значения соответствуют ключам", map.values().equals(List.of("five", "TEN", "fifteen")));
        check("инварианты КЧ-дерева соблюдены", map.isValidRedBlackTree());
    }

    private static void testNullHandling() {
        MyTreeMap<String, Integer> map = new MyTreeMap<>();
        boolean putFailed = false;
        boolean getFailed = false;
        boolean removeFailed = false;

        try {
            map.put(null, 1);
        } catch (NullPointerException expected) {
            putFailed = true;
        }

        try {
            map.get(null);
        } catch (NullPointerException expected) {
            getFailed = true;
        }

        try {
            map.remove(null);
        } catch (NullPointerException expected) {
            removeFailed = true;
        }

        check("put(null, v) бросает NPE", putFailed);
        check("get(null) бросает NPE", getFailed);
        check("remove(null) бросает NPE", removeFailed);

        map.put("empty", null);
        check("null-значение допускается: containsKey == true", map.containsKey("empty"));
        check("null-значение допускается: get == null", map.get("empty") == null);
        check("удаление ключа с null-значением возвращает null", map.remove("empty") == null);
        check("после удаления ключа с null-значением containsKey == false", !map.containsKey("empty"));
        check("после удаления size == 0", map.size() == 0);
    }

    private static void testSortedInsertions() {
        MyTreeMap<Integer, Integer> asc = new MyTreeMap<>();
        MyTreeMap<Integer, Integer> desc = new MyTreeMap<>();
        int n = 1000;

        for (int i = 1; i <= n; i++) {
            asc.put(i, i);
        }
        for (int i = n; i >= 1; i--) {
            desc.put(i, i);
        }

        int maxAllowedHeight = (int) Math.floor(2 * (Math.log(n + 1) / Math.log(2)));
        check("возрастающая вставка: инварианты КЧ-дерева", asc.isValidRedBlackTree());
        check("возрастающая вставка: высота в норме (" + asc.height() + " <= " + maxAllowedHeight + ")",
                asc.height() <= maxAllowedHeight);
        check("убывающая вставка: инварианты КЧ-дерева", desc.isValidRedBlackTree());
        check("убывающая вставка: высота в норме (" + desc.height() + " <= " + maxAllowedHeight + ")",
                desc.height() <= maxAllowedHeight);
        check("порядок ключей корректный", isSorted(asc.keys()) && isSorted(desc.keys()));
    }

    private static void testCustomComparator() {
        MyTreeMap<Integer, String> map = new MyTreeMap<>(Comparator.reverseOrder());
        map.put(1, "one");
        map.put(3, "three");
        map.put(2, "two");

        check("обратный компаратор сортирует по убыванию", map.keys().equals(List.of(3, 2, 1)));
        check("инварианты с кастомным компаратором", map.isValidRedBlackTree());
    }

    private static void testClear() {
        MyTreeMap<Integer, Integer> map = new MyTreeMap<>();
        for (int i = 0; i < 30; i++) {
            map.put(i, i * 10);
        }
        map.clear();

        check("clear обнуляет размер", map.size() == 0 && map.isEmpty());
        check("clear очищает список ключей", map.keys().isEmpty());
        map.put(7, 70);
        check("после clear дерево корректно принимает новые элементы", map.size() == 1 && map.get(7) == 70);
    }

    private static void testRemoveEdgeCases() {
        MyTreeMap<Integer, String> map = new MyTreeMap<>();
        check("удаление из пустой карты возвращает null", map.remove(1) == null);

        map.put(10, "root");
        check("удаление несуществующего ключа возвращает null", map.remove(99) == null);
        check("размер после неудачного удаления не изменился", map.size() == 1);

        check("удаление единственного элемента (корня) возвращает его значение", "root".equals(map.remove(10)));
        check("после удаления корня карта пуста", map.isEmpty() && map.isValidRedBlackTree());
    }

    private static void testRemoveStructuralCases() {
        MyTreeMap<Integer, String> map = new MyTreeMap<>();
        int[] keys = {20, 10, 30, 5, 15, 25, 35, 3, 7, 13, 17};
        for (int k : keys) {
            map.put(k, "v" + k);
        }

        // Удаление листа
        check("удаление листа (3)", "v3".equals(map.remove(3)) && map.isValidRedBlackTree());
        // Удаление узла с одним потомком
        check("удаление узла с одним потомком (5)", "v5".equals(map.remove(5)) && map.isValidRedBlackTree());
        // Удаление узла с двумя потомками
        check("удаление узла с двумя потомками (10)", "v10".equals(map.remove(10)) && map.isValidRedBlackTree());
        // Удаление корня с двумя поддеревьями
        check("удаление корня (20)", "v20".equals(map.remove(20)) && map.isValidRedBlackTree());

        check("повторное удаление уже удалённого ключа (20) -> null", map.remove(20) == null);
        check("оставшиеся ключи в строгом порядке",
                map.keys().equals(List.of(7, 13, 15, 17, 25, 30, 35)));
    }

    private static void testSequentialDeletions() {
        MyTreeMap<Integer, Integer> map = new MyTreeMap<>();
        int n = 400;

        for (int i = 1; i <= n; i++) {
            map.put(i, i);
        }
        boolean validForward = true;
        for (int i = 1; i <= n; i++) {
            if (!Objects.equals(map.remove(i), i) || !map.isValidRedBlackTree()) {
                validForward = false;
                break;
            }
        }
        check("последовательное удаление 1..400 сохраняет инварианты", validForward && map.isEmpty());

        for (int i = 1; i <= n; i++) {
            map.put(i, i);
        }
        boolean validBackward = true;
        for (int i = n; i >= 1; i--) {
            if (!Objects.equals(map.remove(i), i) || !map.isValidRedBlackTree()) {
                validBackward = false;
                break;
            }
        }
        check("обратное удаление 400..1 сохраняет инварианты", validBackward && map.isEmpty());
    }

    private static void testRandomPutAndRemoveAgainstJdk() {
        Random random = new Random(123);
        MyTreeMap<Integer, Integer> custom = new MyTreeMap<>();
        TreeMap<Integer, Integer> standard = new TreeMap<>();
        boolean operationsMatch = true;
        boolean alwaysValid = true;

        for (int i = 0; i < 30_000; i++) {
            int key = random.nextInt(1500);
            if (random.nextBoolean()) {
                int val = random.nextInt();
                if (!Objects.equals(custom.put(key, val), standard.put(key, val))) {
                    operationsMatch = false;
                }
            } else {
                if (!Objects.equals(custom.remove(key), standard.remove(key))) {
                    operationsMatch = false;
                }
            }

            if (i % 250 == 0 && !custom.isValidRedBlackTree()) {
                alwaysValid = false;
            }
        }

        check("стресс-тест put/remove: возвращаемые значения совпадают с JDK", operationsMatch);
        check("стресс-тест put/remove: инварианты соблюдались на всём пути", alwaysValid);
        check("стресс-тест put/remove: финальные инварианты КЧ-дерева", custom.isValidRedBlackTree());
        check("стресс-тест put/remove: размеры совпадают", custom.size() == standard.size());
        check("стресс-тест put/remove: ключи совпадают", custom.keys().equals(new ArrayList<>(standard.keySet())));
        check("стресс-тест put/remove: значения совпадают", custom.values().equals(new ArrayList<>(standard.values())));
    }

    private static boolean isSorted(List<Integer> list) {
        List<Integer> copy = new ArrayList<>(list);
        Collections.sort(copy);
        return copy.equals(list);
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[OK]   " + description);
        } else {
            failed++;
            System.out.println("[FAIL] " + description);
        }
    }
}