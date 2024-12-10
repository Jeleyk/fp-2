# Лабораторная работа №2: Функциональное программирование

**Студент**: Долгих Александр Алексеевич

**Группа**: P34092

**Вариант**: OpenAddress Hashmap Bag(multiset)

## Цель работы

Освоиться с построением пользовательских типов данных, полиморфизмом, рекурсивными алгоритмами и средствами
тестирования (unit testing, property-based testing).

## Требования

1. Функции:
    * добавление и удаление элементов;
    * фильтрация;
    * отображение (map);
    * свертки (левая и правая);
    * структура должна быть моноидом.
2. Структуры данных должны быть неизменяемыми.
3. Библиотека должна быть протестирована в рамках unit testing.
4. Библиотека должна быть протестирована в рамках property-based тестирования (как минимум 3 свойства, включая свойства
   моноида).
5. Структура должна быть полиморфной.
6. Требуется использовать идиоматичный для технологии стиль программирования. Примечание: некоторые языки позволяют
   получить большую часть API через реализацию небольшого интерфейса. Так как лабораторная работа про ФП, а не про
   экосистему языка -- необходимо реализовать их вручную и по возможности -- обеспечить совместимость.

## Описание реализации

Объявление протокола структуры данных:

```clojure
(defprotocol IOpenAddressHashMapBag
   (insert [this x])
   (delete [this x])
   (contains [this x])
   (cnt [this x])
   (mapb [this f])
   (foldl [this f init])
   (foldr [this f init])
   (combine [this other]))
```

Реализация структуры данных, согласно протоколу:

```clojure
(deftype OpenAddressHashMapBag [table size]
   IOpenAddressHashMapBag
   (insert [_ x] (OpenAddressHashMapBag. (insert-bag table size x) size))
   (delete [_ x] (OpenAddressHashMapBag. (remove-bag table size x) size))
   (contains [_ x] (contains-bag? table size x))
   (cnt [_ x] (get-count table size x))
   (mapb [_ f] (OpenAddressHashMapBag. (map-bag f table size) size))
   (foldl [_ f acc] (foldl-bag f acc table))
   (foldr [_ f acc] (foldl-bag f acc (reverse table)))
   (combine [t other] (foldl t insert other))

   ISeq
   (seq [_]
      (let [non-nil-slots (filter #(and % (> (second %) 0)) table)]
         (map (fn [[k count]] (repeat count k)) non-nil-slots)))

   )
```

Функция для создания структуры данных:

```clojure
(defn bag [& args]
   (reduce #(insert %1 %2) (OpenAddressHashMapBag. (vec (repeat max-size nil)) max-size) args))
```

## Разработанное тестовое покрытие

### Unit тесты

```clojure
(deftest test-insert
         (let [b (bag)]
            (is (= 0 (cnt b 1)))
            (let [b2 (insert b 1)]
               (is (= 1 (cnt b2 1)))
               (let [b3 (insert b2 1)]
                  (is (= 2 (cnt b3 1)))))))

(deftest test-delete
         (let [b (bag 1 1 2)]
            (is (= 2 (cnt b 1)))
            (is (= 1 (cnt b 2)))
            (let [b2 (delete b 1)]
               (is (= 1 (cnt b2 1)))
               (is (= 1 (cnt b2 2)))
               (let [b3 (delete b2 1)]
                  (is (= 0 (cnt b3 1)))
                  (is (= 1 (cnt b3 2)))))))

(deftest test-contains
         (let [b (bag 1 2 3)]
            (is (contains b 1))
            (is (contains b 2))
            (is (contains b 3))
            (is (not (contains b 4)))))

(deftest test-map
         (let [b (bag 1 2 3)
               b2 (mapb b inc)]
            (is (contains b2 2))
            (is (contains b2 3))
            (is (contains b2 4))
            (is (not (contains b2 1)))))

(deftest test-foldl
         (let [b (bag 1 2 2 3)]
            (is (= 8 (foldl b + 0)))))

(deftest test-foldr
         (let [b (bag 1 2 2 3)]
            (is (= 8 (foldr b + 0)))))

(deftest test-combine
         (let [b1 (bag 1 2 2)
               b2 (bag 2 3)
               b3 (combine b1 b2)]
            (is (= 1 (cnt b3 1)))
            (is (= 3 (cnt b3 2)))
            (is (= 1 (cnt b3 3)))))

(deftest test-integration
         (let [b (bag 1 2 2 3)
               b2 (insert b 4)
               b3 (delete b2 2)
               b4 (combine b3 (bag 5 6))
               b5 (mapb b4 inc)]
            (is (= 1 (cnt b5 2)))
            (is (= 1 (cnt b5 3)))
            (is (= 1 (cnt b5 4)))
            (is (= 1 (cnt b5 5)))
            (is (= 1 (cnt b5 6)))
            (is (= 1 (cnt b5 7)))))


```

### Property-based тесты

```clojure
(defspec monoid-associativity
         100
         (prop/for-all [a bag-gen
                        b bag-gen
                        c bag-gen]
                       (let [ab (combine a b)
                             bc (combine b c)
                             left (combine ab c)
                             right (combine a bc)]
                          (every? (fn [x] (= (cnt left x) (cnt right x)))
                                  (range 100)))))

(defspec monoid-identity
         100
         (prop/for-all [a bag-gen]
                       (let [empty-bag (bag)]
                          (and (every? (fn [x] (= (cnt (combine a empty-bag) x) (cnt a x)))
                                       (range 100))
                               (every? (fn [x] (= (cnt (combine empty-bag a) x) (cnt a x)))
                                       (range 100))))))

(defspec insertion-and-deletion
         100
         (prop/for-all [a bag-gen
                        x gen/small-integer]
                       (let [with-x (insert a x)
                             without-x (delete with-x x)]
                          (every? (fn [key] (= (cnt without-x key) (cnt a key)))
                                  (range 100)))))

(defspec combine-commutativity
         100
         (prop/for-all [a bag-gen
                        b bag-gen]
                       (let [ab (combine a b)
                             ba (combine b a)]
                          (every? (fn [x] (= (cnt ab x) (cnt ba x)))
                                  (range 100)))))
```

## Заключение

В ходе выполнения лабораторной работы были реализованы основные функции для работы с OpenAddress Hashmap, реализован Bag. Структура
данных была протестирована с использованием unit и property-based тестов. Реализация показала, что использование
неизменяемых структур позволяет легко сохранять целостность данных и упрощает тестирование.