(ns monad.fn-test
  (:require [clojure.test :refer :all]
            [monad.fn :refer :all]))

(defn f [x] (+ x x))
(defn g [n] (+ 1 n))
(defn h [x] 5)

(defn mf [x] (unit (f x)))
(defn mg [n] (unit (g n)))
(defn mh [x] (unit (h x)))

(deftest test-monad-laws-assoc
  (testing "Associativity law: μ ∘ Tμ = μ ∘ μT"
    (is (= ((bind (bind (unit 3) mf) mg))
           ((bind (unit 3) (fn [x] (bind (mf x) mg))))))))

(deftest test-monad-laws-identity
  (testing "Identity law: μ ∘ Tη = μ ∘ ηT = idT"
    (is (= (eval (bind (unit 3) f))
           (eval (f 3))))
    (is (= ((bind (unit 3) unit))
           ((unit 3))))))
