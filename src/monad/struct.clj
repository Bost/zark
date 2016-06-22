(ns monad.struct
  {:lang :core.typed}
  (:require [clojure.core.typed :as t]))

;; Monad - Kleisli triple (T,η,μ):
;; Endofunctor T: C -> C (type conctructor)
;; Natural Transf Etha η: idC -> T (unit function)
;; Natural Transf Mi   μ: T^2 -> T (bind operation)

;; 1. t/Str and String are the same: java.lang.String
;; 2. * means: any number of params of type t/Any
;; (t/defalias t t/Any)
;; (t/defalias Mt (t/HVec [t/Any]))

;; (t/ann type-constructor [t -> Mt])
;; (def type-constructor
;;   "endofunctor T: C -> C;  - t, Mt are objects of Category C"
;;   clojure.core/vector)

;; (t/ann unit [t -> Mt])
;; (defn unit
;;   "η: idC -> T; idC is an identity functor on C"
;;   [n]
;;   (type-constructor n))

(t/defalias t t/Int)
(t/defalias k clojure.lang.Keyword)

(t/defalias Mt
  #_{:sum 0 :zins 0 :lauf 0}
  (t/HMap :mandatory {:sum t
                      :zins t
                      :lauf t} :complete? true))

(def type-constructor
  "Endofunctor T: C -> C; t, Mt are objects of Category C"
  (t/fn [s :- t z :- t l :- t] :- Mt
    ;; this doesn't work
    #_(clojure.core/hash-map :sum s :zins z :lauf l)
    {:sum s :zins z :lauf l}))

(t/ann unit [t t t -> Mt])
(defn unit [s z l]
  "η: idC -> T; idC is an identity functor on C"
  (type-constructor s z l))

(t/ann bind [Mt [t t t -> Mt] -> Mt])
(defn bind "μ: T^2 -> T" [mv f] (f (:sum mv) (:zins mv) (:lauf mv)))

(t/ann laufcalc [t t -> t])
(defn laufcalc [lauf n] (+ lauf (* n 12)))

(t/ann mlauf [Mt t -> Mt])
(defn mlauf [mv n]
  (bind mv (t/fn [s :- t z :- t l :- t]
             (type-constructor s z (laufcalc l n)))))

(t/ann zinscalc [t t -> t])
(defn zinscalc [zins n] (+ zins n))

(t/ann mzins [Mt t -> Mt])
(defn mzins [mv n]
  (bind mv (t/fn [s :- t z :- t l :- t]
             (type-constructor s (zinscalc z n) l))))

;; (def c1 {:sum 100 :zins 50 :lauf 12})
#_(t/ann c2 Mt)
(def c2 {:sum 200 :zins 60 :lauf 24})

(-> c2
    (mzins 2)
    (mlauf 1))
