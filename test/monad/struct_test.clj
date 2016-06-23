(ns monad.struct-test
  (:require [clojure.test :refer :all]
            [monad.struct :refer :all]))

(def s 200)
(def z 60)
(def l 24)

(def x 1)
(def y 2)

(deftest test-monad-laws-assoc
  (testing "Associativity law: μ ∘ Tμ = μ ∘ μT"
    (is (= (bind (bind (unit s z l)
                       (fn [ss zz ll] (mzins ss zz ll x)))
                 (fn [ss zz ll] (mlauf ss zz ll y)))
           (bind (unit s z l)
                 (fn [ss zz ll]
                   (bind (mzins ss zz ll x)
                         (fn [sss zzz lll] (mlauf sss zzz lll y)))))))))



(deftest test-monad-laws-identity
  (testing "Identity law: μ ∘ Tη = μ ∘ ηT = idT"
    ;; (unit x) >>= f ≡ f x
    (is (= (bind (unit s z l) (fn [ss zz ll] (mzins ss zz ll x)))
           (mzins s z l x)))
    ;; m >>= return ≡ m
    (is (= (bind (TypeConstructor s z l) unit)
           (TypeConstructor s z l)))))
