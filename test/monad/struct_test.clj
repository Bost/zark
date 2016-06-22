(ns monad.struct-test
  (:require [clojure.test :refer :all]
            [monad.struct :refer :all]))

;; (defn f [x] (+ x x))
;; (defn g [n] (+ 1 n))
;; (defn h [x] 5)

;; (defn mf [x] (unit (f x)))
;; (defn mg [n] (unit (g n)))
;; (defn mh [x] (unit (h x)))

;; (def c2 {:sum 200 :zins 60 :lauf 24})


(bind (bind c2
            (mlauf (:sum c2) (:zins c2) (:lauf c2) 1))
      (mzins (:sum c2) (:zins c2) (:lauf c2) 2))

(deftest test-monad-laws-assoc
  (testing "Associativity law: μ ∘ Tμ = μ ∘ μT"
    (is (= (bind (bind c2 (mlauf (:sum c2) (:zins c2) (:lauf c2) 1)) (mzins (:sum c2) (:zins c2) (:lauf c2) 2))
           (bind c2 (fn [mv x] (bind (mlauf mv 1) (mzins mv 2))))
           ))))

(deftest test-monad-laws-identity
  (testing "Identity law: μ ∘ Tη = μ ∘ ηT = idT"
    (is (= (eval (bind c2 (mlauf c2 1)))
           (eval (mlauf c2 1))))
    (is (= (bind c2 unit)
           c2))))

