(ns ru.jeleyka.functionalprogramming.lab2.oa-bag
  (:import (clojure.lang ISeq)))

(defn- hash-fn [x size]
  (mod (hash x) size))

(defn- find-insert-slot [table size key]
  (let [h (hash-fn key size)]
    (loop [i h, checked 0]
      (if (>= checked size)
        nil
        (let [slot (get table i)]
          (if (nil? slot)
            i
            (if (<= (second slot) 0)
              i
              (if (= (compare (first slot) key) 0)
                i
                (recur (mod (+ i 1) size) (inc checked))))))))))

(defn- find-slot [table size key]
  (let [h (hash-fn key size)]
    (loop [i h, checked 0]
      (if (>= checked size)
        nil
        (let [slot (get table i)]
          (if (nil? slot)
            i
            (if (and (= (compare (first slot) key) 0) (> (second slot) 0))
              i
              (recur (mod (+ i 1) size) (inc checked)))))))))

(defn- insert-bag [table size key]
  (let [slot (find-insert-slot table size key)]
    (if (nil? (get table slot))
      (assoc table slot [key 1])
      (let [[k count] (get table slot)]
        (assoc table slot [k (inc count)])))))

(defn- remove-bag [table size key]
  (let [slot (find-slot table size key)]
    (if (nil? (get table slot))
      table
      (let [[k count] (get table slot)]
        (assoc table slot [k (max (dec count) -1)])))))

(defn- contains-bag? [table size key]
  (let [slot (find-slot table size key)]
    (some? (get table slot))))

(defn- get-count [table size key]
  (let [slot (find-slot table size key)]
    (if (nil? (get table slot))
      0
      (second (get table slot)))))

(defn- map-bag [f table size]
  (let [new-table (vec (repeat size nil))]
    (reduce
      (fn [acc slot]
        (if (and slot (> (second slot) 0))
          (let [[key count] slot
                new-key (f key)]
            (loop [i 0, acc acc]
              (if (>= i count)
                acc
                (let [acc (insert-bag acc size new-key)]
                  (recur (inc i) acc)))))
          acc))
      new-table
      table)))

(defn- foldl-bag [f acc table]
  (reduce
    (fn [acc slot]
      (if (and slot (> (second slot) 0))
        (let [[key count] slot]
          (loop [i 0, acc acc]
            (if (>= i count)
              acc
              (recur (inc i) (f acc key)))))
        acc))
    acc
    table))

(defprotocol IOpenAddressHashMapBag
  (insert [this x])
  (delete [this x])
  (contains [this x])
  (cnt [this x])
  (mapb [this f])
  (foldl [this f init])
  (foldr [this f init])
  (combine [this other]))

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

(def max-size 256)

(defn bag [& args]
  (reduce #(insert %1 %2) (OpenAddressHashMapBag. (vec (repeat max-size nil)) max-size) args))

(def my-bag (bag 1 2 2 3 3 3))