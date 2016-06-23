(ns monad.vector-test
  (:require [clojure.test :refer :all]
            [monad.vector :refer :all]))

(defn f [x] (+ x x))
(defn g [n] (+ 1 n))
(defn h [x] 5)

(defn mf [x] (unit (f x)))
(defn mg [n] (unit (g n)))
(defn mh [x] (unit (h x)))

(deftest test-monad-laws-assoc
  (testing "Associativity law: μ ∘ Tμ = μ ∘ μT"
    (is (= (bind (bind (unit 3) mf) mg)
           (bind (unit 3) (fn [x] (bind (mf x) mg)))))))

(deftest test-monad-laws-identity
  (testing "Identity law: μ ∘ Tη = μ ∘ ηT = idT"
    ;; (unit x) >>= f ≡ f x
    (is (= (bind (unit 3) mf)
           (mf 3)))
    ;; m >>= return ≡ m
    (is (= (bind (TypeConstructor 3) unit)
           (TypeConstructor 3)))))
