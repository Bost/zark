(ns monad.id
  {:lang :core.typed}
  (:require [clojure.core.typed :as t]))

;; A Monad on category C: Kleisli triple (T,η,μ):
;; Endofunctor T: C -> C (type conctructor)
;; Natural Transf Etha η: idC -> T (unit function)
;; Natural Transf Mi   μ: T^2 -> T (bind operation)

;; 1. Identity laws (Nat Trans T -> T):       μ ∘ Tη = μ ∘ ηT = idT
;; 1.1.
;; (= (bind (unit x) f) (f x))
;; 1.2. complement
;; (= (bind mv unit) mv)

;; 2. Associativity law (Nat Trans T^3 -> T): μ ∘ Tμ = μ ∘ μT
;; (= (bind (bind mv f) g)
;;    (bind mv (fn [x] (bind (f x) g))))


;; 1. t/Str and String are the same: java.lang.String
;; 2. * means: any number of params of type t/Any

;; "Endofunctor T: C -> C; t, Mt are objects of Category C"
(t/defalias t t/Any)
(t/defalias Mt t/Any)

(t/ann TypeConstructor [t -> Mt])
(defn TypeConstructor [t] t)

(t/ann unit [t -> Mt])   ;; Fn -> Vec
(defn unit
  "η: idC -> T; idC is an identity functor on C"
  [n]
  (TypeConstructor n))

(t/ann bind [Mt [t -> Mt] -> Mt])
(defn bind "μ: T^2 -> T" [mv f] (f mv))

(t/ann testf [t -> Mt])
(defn testf [x] x (unit x))
