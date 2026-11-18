import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

public class Main {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== День 4: MyBidiTreeMap — двунаправленная карта ===\n");

        // Регрессионные тесты MyTreeMap (Дни 1–3)
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
        testFirstAndLastNavigation();
        testFloorLowerCeilingHigher();
        testSubMapHeadMapTailMap();
        testNavigationAgainstJdk();

        // Тесты двунаправленной карты (День 4)
        testBidiBasicOperations();
        testBidiDuplicateValueReplacesOldLink();
        testBidiRemoveAndRemoveByValue();
        testBidiInverseViewSync();
        testBidiNullsAndComparators();
        testBidiStressConsistency();

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
        Comparator<Integer> reverse = Comparator.reverseOrder();
        MyTreeMap<Integer, String> map = new MyTreeMap<>(reverse);
        map.put(1, "one");
        map.put(3, "three");
        map.put(2, "two");

        check("обратный компаратор сортирует по убыванию", map.keys().equals(List.of(3, 2, 1)));
        check("comparator() возвращает переданный компаратор", map.comparator() == reverse);
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

        check("удаление листа (3)", "v3".equals(map.remove(3)) && map.isValidRedBlackTree());
        check("удаление узла с одним потомком (5)", "v5".equals(map.remove(5)) && map.isValidRedBlackTree());
        check("удаление узла с двумя потомками (10)", "v10".equals(map.remove(10)) && map.isValidRedBlackTree());
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

    private static void testFirstAndLastNavigation() {
        MyNavigableMap<Integer, String> map = new MyTreeMap<>();

        boolean firstThrows = false;
        boolean lastThrows = false;
        try {
            map.firstKey();
        } catch (NoSuchElementException expected) {
            firstThrows = true;
        }
        try {
            map.lastKey();
        } catch (NoSuchElementException expected) {
            lastThrows = true;
        }

        check("пустая карта: firstKey() бросает NoSuchElementException", firstThrows);
        check("пустая карта: lastKey() бросает NoSuchElementException", lastThrows);
        check("пустая карта: firstEntry() == null", map.firstEntry() == null);
        check("пустая карта: lastEntry() == null", map.lastEntry() == null);

        map.put(50, "fifty");
        map.put(20, "twenty");
        map.put(80, "eighty");
        map.put(10, "ten");
        map.put(90, "ninety");

        check("firstKey() == 10", Integer.valueOf(10).equals(map.firstKey()));
        check("lastKey() == 90", Integer.valueOf(90).equals(map.lastKey()));
        check("firstEntry() -> 10=ten",
                map.firstEntry().getKey() == 10 && "ten".equals(map.firstEntry().getValue()));
        check("lastEntry() -> 90=ninety",
                map.lastEntry().getKey() == 90 && "ninety".equals(map.lastEntry().getValue()));
    }

    private static void testFloorLowerCeilingHigher() {
        MyNavigableMap<Integer, String> map = new MyTreeMap<>();
        for (int k : new int[]{10, 20, 30, 40, 50}) {
            map.put(k, "v" + k);
        }

        check("floorKey(30) == 30", Integer.valueOf(30).equals(map.floorKey(30)));
        check("lowerKey(30) == 20", Integer.valueOf(20).equals(map.lowerKey(30)));
        check("ceilingKey(30) == 30", Integer.valueOf(30).equals(map.ceilingKey(30)));
        check("higherKey(30) == 40", Integer.valueOf(40).equals(map.higherKey(30)));

        check("floorKey(25) == 20", Integer.valueOf(20).equals(map.floorKey(25)));
        check("lowerKey(25) == 20", Integer.valueOf(20).equals(map.lowerKey(25)));
        check("ceilingKey(25) == 30", Integer.valueOf(30).equals(map.ceilingKey(25)));
        check("higherKey(25) == 30", Integer.valueOf(30).equals(map.higherKey(25)));

        check("floorKey(5) == null", map.floorKey(5) == null);
        check("lowerKey(10) == null", map.lowerKey(10) == null);
        check("ceilingKey(55) == null", map.ceilingKey(55) == null);
        check("higherKey(50) == null", map.higherKey(50) == null);
    }

    private static void testSubMapHeadMapTailMap() {
        MyNavigableMap<Integer, String> map = new MyTreeMap<>();
        for (int k : new int[]{10, 20, 30, 40, 50}) {
            map.put(k, "v" + k);
        }

        MyNavigableMap<Integer, String> halfOpen = map.subMap(20, 40);
        check("subMap(20, 40) -> [20, 30]", halfOpen.keys().equals(List.of(20, 30)));

        MyNavigableMap<Integer, String> closed = map.subMap(20, true, 40, true);
        check("subMap(20, true, 40, true) -> [20, 30, 40]", closed.keys().equals(List.of(20, 30, 40)));

        MyNavigableMap<Integer, String> open = map.subMap(10, false, 50, false);
        check("subMap(10, false, 50, false) -> [20, 30, 40]", open.keys().equals(List.of(20, 30, 40)));

        check("subMap(25, 25) пуст", map.subMap(25, 25).isEmpty());

        boolean badRangeThrows = false;
        try {
            map.subMap(40, 20);
        } catch (IllegalArgumentException expected) {
            badRangeThrows = true;
        }
        check("subMap(40, 20) бросает IllegalArgumentException", badRangeThrows);

        check("headMap(30, false) -> [10, 20]", map.headMap(30, false).keys().equals(List.of(10, 20)));
        check("headMap(30, true) -> [10, 20, 30]", map.headMap(30, true).keys().equals(List.of(10, 20, 30)));
        check("tailMap(30, false) -> [40, 50]", map.tailMap(30, false).keys().equals(List.of(40, 50)));
        check("tailMap(30, true) -> [30, 40, 50]", map.tailMap(30, true).keys().equals(List.of(30, 40, 50)));

        halfOpen.put(25, "v25");
        check("изменение subMap не затрагивает исходную карту", !map.containsKey(25));
    }

    private static void testNavigationAgainstJdk() {
        Random random = new Random(777);
        MyTreeMap<Integer, Integer> custom = new MyTreeMap<>();
        TreeMap<Integer, Integer> standard = new TreeMap<>();

        for (int i = 0; i < 300; i++) {
            int k = random.nextInt(1000);
            custom.put(k, k * 2);
            standard.put(k, k * 2);
        }

        boolean neighborsMatch = true;
        for (int probe = -10; probe <= 1010; probe++) {
            if (!Objects.equals(custom.floorKey(probe), standard.floorKey(probe))
                    || !Objects.equals(custom.lowerKey(probe), standard.lowerKey(probe))
                    || !Objects.equals(custom.ceilingKey(probe), standard.ceilingKey(probe))
                    || !Objects.equals(custom.higherKey(probe), standard.higherKey(probe))) {
                neighborsMatch = false;
                break;
            }
        }

        check("сравнение с JDK: firstKey и lastKey совпадают",
                Objects.equals(custom.firstKey(), standard.firstKey())
                        && Objects.equals(custom.lastKey(), standard.lastKey()));
        check("сравнение с JDK: floor/lower/ceiling/higher совпадают для всех точек", neighborsMatch);
        check("сравнение с JDK: subMap(200, true, 750, false) совпадает",
                custom.subMap(200, true, 750, false).keys()
                        .equals(new ArrayList<>(standard.subMap(200, true, 750, false).keySet())));
        check("сравнение с JDK: headMap(400, true) совпадает",
                custom.headMap(400, true).keys()
                        .equals(new ArrayList<>(standard.headMap(400, true).keySet())));
        check("сравнение с JDK: tailMap(600, false) совпадает",
                custom.tailMap(600, false).keys()
                        .equals(new ArrayList<>(standard.tailMap(600, false).keySet())));
    }

    // ------------------------------------------------- Тесты MyBidiTreeMap

    private static void testBidiBasicOperations() {
        MyBidiTreeMap<Integer, String> bidi = new MyBidiTreeMap<>();
        check("BidiMap: изначально пуста", bidi.isEmpty() && bidi.size() == 0);

        bidi.put(1, "USD");
        bidi.put(2, "EUR");
        bidi.put(3, "GBP");

        check("BidiMap: size == 3", bidi.size() == 3);
        check("BidiMap: get(2) == EUR", "EUR".equals(bidi.get(2)));
        check("BidiMap: getKey(EUR) == 2", Integer.valueOf(2).equals(bidi.getKey("EUR")));
        check("BidiMap: containsValue(GBP) == true", bidi.containsValue("GBP"));
        check("BidiMap: containsValue(JPY) == false", !bidi.containsValue("JPY"));
        check("BidiMap: keys() отсортированы", bidi.keys().equals(List.of(1, 2, 3)));
        check("BidiMap: values() в порядке ключей", bidi.values().equals(List.of("USD", "EUR", "GBP")));
        check("BidiMap: деревья согласованы", bidi.isConsistent());

        bidi.clear();
        check("BidiMap: clear очищает оба направления",
                bidi.isEmpty() && bidi.getKey("USD") == null && bidi.isConsistent());
    }

    private static void testBidiDuplicateValueReplacesOldLink() {
        MyBidiTreeMap<Integer, String> bidi = new MyBidiTreeMap<>();
        bidi.put(10, "Admin");
        bidi.put(20, "Editor");

        // Привязываем существующее значение "Admin" к новому ключу 30 — старый ключ 10 должен удалиться
        String prev = bidi.put(30, "Admin");
        check("BidiMap: вставка нового ключа с занятым значением возвращает null", prev == null);
        check("BidiMap: старый ключ (10) автоматически удалён", !bidi.containsKey(10));
        check("BidiMap: getKey(Admin) теперь возвращает 30", Integer.valueOf(30).equals(bidi.getKey("Admin")));
        check("BidiMap: размер остался 2", bidi.size() == 2 && bidi.isConsistent());

        // Перезаписываем существующий ключ 20 значением "Admin", которое уже занято ключом 30
        String replaced = bidi.put(20, "Admin");
        check("BidiMap: перезапись ключа возвращает его старое значение (Editor)", "Editor".equals(replaced));
        check("BidiMap: старое значение (Editor) больше не числится в карте", !bidi.containsValue("Editor"));
        check("BidiMap: прежний владелец (30) удалён", !bidi.containsKey(30));
        check("BidiMap: осталась единственная связь 20 <-> Admin",
                bidi.size() == 1 && Integer.valueOf(20).equals(bidi.getKey("Admin")) && bidi.isConsistent());
    }

    private static void testBidiRemoveAndRemoveByValue() {
        MyBidiTreeMap<String, Integer> bidi = new MyBidiTreeMap<>();
        bidi.put("alpha", 100);
        bidi.put("beta", 200);
        bidi.put("gamma", 300);

        check("BidiMap: remove(beta) возвращает 200", Integer.valueOf(200).equals(bidi.remove("beta")));
        check("BidiMap: после remove(beta) значение 200 удалено из обратной карты",
                !bidi.containsValue(200) && bidi.getKey(200) == null);

        check("BidiMap: removeByValue(300) возвращает gamma", "gamma".equals(bidi.removeByValue(300)));
        check("BidiMap: после removeByValue(300) ключ gamma удалён из прямой карты",
                !bidi.containsKey("gamma") && bidi.get("gamma") == null);

        check("BidiMap: removeByValue(999) -> null", bidi.removeByValue(999) == null);
        check("BidiMap: размер == 1 и деревья согласованы", bidi.size() == 1 && bidi.isConsistent());
    }

    private static void testBidiInverseViewSync() {
        MyBidiTreeMap<Integer, String> codes = new MyBidiTreeMap<>();
        codes.put(404, "Not Found");
        codes.put(200, "OK");

        MyBidiMap<String, Integer> byText = codes.inverse();
        check("BidiMap: inverse().inverse() возвращает исходный объект", byText.inverse() == codes);
        check("BidiMap: inverse().get(OK) == 200", Integer.valueOf(200).equals(byText.get("OK")));
        check("BidiMap: inverse().getKey(404) == Not Found", "Not Found".equals(byText.getKey(404)));

        // Изменяем через обратное представление и проверяем отражение в прямом
        byText.put("Created", 201);
        check("BidiMap: вставка в inverse() видна в основной карте",
                "Created".equals(codes.get(201)) && codes.size() == 3);

        byText.remove("Not Found");
        check("BidiMap: удаление из inverse() удаляет запись из основной карты",
                !codes.containsKey(404) && codes.isConsistent());
    }

    private static void testBidiNullsAndComparators() {
        MyBidiTreeMap<String, String> bidi = new MyBidiTreeMap<>(
                String.CASE_INSENSITIVE_ORDER,
                Comparator.reverseOrder()
        );
        bidi.put("b", "second");
        bidi.put("A", "first");
        bidi.put("c", "third");

        check("BidiMap: кастомный компаратор ключей работает", bidi.keys().equals(List.of("A", "b", "c")));
        check("BidiMap: кастомный компаратор значений работает в inverse()",
                bidi.inverse().keys().equals(List.of("third", "second", "first")));

        boolean nullKeyThrows = false;
        boolean nullValueThrows = false;
        try {
            bidi.put(null, "x");
        } catch (NullPointerException expected) {
            nullKeyThrows = true;
        }
        try {
            bidi.put("x", null);
        } catch (NullPointerException expected) {
            nullValueThrows = true;
        }
        check("BidiMap: put(null, v) бросает NPE", nullKeyThrows);
        check("BidiMap: put(k, null) бросает NPE", nullValueThrows);
    }

    private static void testBidiStressConsistency() {
        Random random = new Random(999);
        MyBidiTreeMap<Integer, Integer> bidi = new MyBidiTreeMap<>();
        boolean allConsistent = true;

        for (int i = 0; i < 10_000; i++) {
            int action = random.nextInt(4);
            int k = random.nextInt(250);
            int v = random.nextInt(250);

            switch (action) {
                case 0 -> bidi.put(k, v);
                case 1 -> bidi.remove(k);
                case 2 -> bidi.removeByValue(v);
                case 3 -> bidi.inverse().put(v, k);
            }

            if (i % 100 == 0 && !bidi.isConsistent()) {
                allConsistent = false;
                break;
            }
        }

        check("BidiMap стресс-тест: оба дерева оставались согласованными на всём пути",
                allConsistent && bidi.isConsistent());
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