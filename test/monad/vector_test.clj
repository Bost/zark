(ns monad.vector-test
  (:require [clojure.test :refer :all]
            [monad.vector :refer :all]))

(defn f [x] (+ x x))
(defn g [x] (+ 1 x))
(defn h [x] 5)

(defn mf [x] (unit (f x)))
(defn mg [n] (unit (g n)))
(defn mh [x] (unit (h x)))

(deftest test-monad-laws-assoc
  (testing "Associativity law: μ ∘ Tμ = μ ∘ μT"
    (is (= (eval (bind (bind (unit 3) mf) mg))
           (eval (bind (unit 3) (fn [x] (bind (mf x) mg))))))))

(deftest test-monad-laws-identity
  (testing "Identity law: μ ∘ Tη = μ ∘ ηT = idT"
    (is (= (eval (bind (unit 3) f))
           (eval (f 3))))
    (is (= (eval (bind (unit 3) unit))
           (eval (unit 3))))))

