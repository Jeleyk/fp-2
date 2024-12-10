(ns ru.jeleyka.functionalprogramming.lab2.oa-bag-prop-test
  (:require [clojure.test :refer :all]
            [clojure.test.check :refer [quick-check]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [ru.jeleyka.functionalprogramming.lab2.oa-bag :refer :all]))

(def bag-gen
  (gen/fmap #(apply bag %)
            (gen/vector gen/small-integer 10)))

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