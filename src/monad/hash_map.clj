(ns monad.hash-map
  {:lang :core.typed}
  (:require [clojure.core.typed :as t]))

;; Monad - Kleisli triple (T,η,μ):
;; Endofunctor T: C -> C (type conctructor)
;; Natural Transf Etha η: idC -> T (unit function)
;; Natural Transf Mi   μ: T^2 -> T (bind operation)

;; 1. t/Str and String are the same: java.lang.String
;; 2. * means: any number of params of type t/Any
(t/defalias t t/Int)
(t/defalias k clojure.lang.Keyword) ;; TODO use k insead of keywords

(t/defalias Mt
  #_{:sum 0 :zins 0 :lauf 0}
  (t/HMap :mandatory {:sum t
                      :zins t
                      :lauf t} :complete? true))

(t/ann TypeConstructor [t t t -> Mt])
(defn TypeConstructor
  "Endofunctor T: C -> C; t, Mt are objects of Category C"
  [s z l] {:sum s :zins z :lauf l})

(t/ann unit [t t t -> Mt])
(defn unit
  "η: idC -> T; idC is an identity functor on C"
  [s z l] (TypeConstructor s z l))

(t/ann bind [Mt [t t t -> Mt] -> Mt])
(defn bind "μ: T^2 -> T" [mv f]
  (f (:sum mv) (:zins mv) (:lauf mv)))

(t/ann laufcalc [t t -> t])
(defn laufcalc [l n] (+ l (* n 12)))

(t/ann mlauf [t t t t -> Mt])
(defn mlauf [s z l n]
  (unit s z (laufcalc l n)))

(t/ann bmlauf [Mt t -> Mt])
(defn bmlauf [mv n]
  (bind mv (t/fn [s :- t z :- t l :- t] (mlauf s z l n))))

(t/ann zinscalc [t t -> t])
(defn zinscalc [z n] (+ z n))

(t/ann mzins [t t t t -> Mt])
(defn mzins [s z l n]
  (unit s (zinscalc z n) l))

(t/ann bmzins [Mt t -> Mt])
(defn bmzins [mv n]
  (bind mv (t/fn [s :- t z :- t l :- t] (mzins s z l n))))

(-> (unit 200 60 24)
    (bmzins 2)
    (bmlauf 1))
