(ns ru.jeleyka.functionalprogramming.lab2.oa-bag-test
  (:require [clojure.test :refer :all]
            [ru.jeleyka.functionalprogramming.lab2.oa-bag :refer :all]))

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

