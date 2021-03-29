(ns monad.vector
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

(defn idc [obj]   obj)
(defn pc  [objp1] 2)
(defn mc  [objp2] 1)

(defn idd [obj]   obj)
(defn pd  [objm1] -2)
(defn md  [objm2] -1)

(defn idf "= εF ∘ Fη" [fcat] fcat)
(defn idg "= Gε ∘ ηG" [gcat] gcat)

(defn pdc [pd] pc)
(defn mdc [md] mc)

(defn pcd [pc] pd)
(defn mcd [mc] md)

(def Ccat {:objs [ 1,  2] :fns  [idc, pc, mc]})
(def Dcat {:objs [-1, -2] :fns  [idd, pd, md]})

(def Ftor "F: D -> C"
  {:objs [Dcat, Ccat] :fns  [idf, pdc, mdc]})

(def Gtor "G: C -> D"
  {:objs [Ccat, Dcat] :fns  [idg, pcd, mcd]})

(defn ε "counit: FG  -> idc " [Fcat_GCat] idc)
(defn η "unit  : idd -> GF"   [idd] (fn [pd] (pcd (pdc pd))))

(defn T
  "= G ∘ F; takes set X & returns an underlying set of the free group Free(X)"
  [setX] nil)


;; 1. t/Str and String are the same: java.lang.String
;; 2. * means: any number of params of type t/Any
(t/defalias t t/Any)
(t/defalias Mt (t/HVec [t/Any]))

(t/ann TypeConstructor [t -> Mt])
(defn TypeConstructor
  "Endofunctor T: C -> C; t, Mt are objects of Category C"
  [t] (clojure.core/vector t))

(t/ann unit [t -> Mt])   ;; Fn -> Vec
(defn unit
  "η: idC -> T; idC is an identity functor on C"
  [n] (TypeConstructor n))

(t/ann bind [Mt [t -> Mt] -> Mt])
(defn bind "μ: T^2 -> T" [mv f]
  (f (first mv)))

;;;; ad-hoc monad & monoid definition:

;; m-result is required. Type signature: m-result: a -> m a
(defn m-result [x] (list x))

;; m-bind is required. Type signature: m-bind: m a -> (a -> m b) -> m b
;; There's no "Extract value from monad (i.e. monadic container)!" in an
;; analogous way as in "There's no *the* value in a collection (i.e. a list)"
(defn m-bind [m-val m-func] (flatten (map m-func m-val)))

;; m-zero is optional; Type signature: m-zero: * -> m a
;; * - kind; i.e. any type(s)
(defn m-zero [& _] (m-result 0))

;; m-plus is optional; Type signature: m-plus: m a -> m a -> m a
;; by defining an associative m-plus operation and it's identity we get a monoid
;; over a set of monadic values
(defn m-plus [& m-vals] (apply map + m-vals))

(defn f [x] (+ x x))
(defn g [n] (+ 1 n))
(defn h [x] 5)

(defn mf [x] (m-result (f x)))
(defn mg [n] (m-result (g n)))
(defn mh [x] (m-result (h x)))

(defn test-monad-laws-identity
  "Identity law: μ ∘ Tη = μ ∘ ηT = idT"
  []
  (and
   ;; left identity law
   (= (m-bind (m-result 3) mf)
      (mf 3))
   ;; right identity law
   (= (m-bind (m-result 3) m-result)
      (m-result 3))

   ;; left identity law with respect to zero
   (= (m-bind (m-zero) mf)
      (mf 0))
   ;; right identity law with respect to zero
   (= (m-bind (m-zero) m-result)
      (m-result 0))

   ;; symetry of plus with respect to zero: a + 0 = 0 + a = a
   (= (m-plus (m-result 3) (m-zero))
      (m-plus (m-zero) (m-result 3))
      (m-result 3))))

(defn test-monad-laws-assoc
  "Associativity law: μ ∘ Tμ = μ ∘ μT"
  []
  (= (m-bind (m-bind (m-result 3) mf) mg)
     (m-bind (m-result 3) (fn [x] (m-bind (mf x) mg))))
  ;; symetry: a + b = b + a
  (= (m-plus (m-result 3) (m-result 6))
     (m-plus (m-result 6) (m-result 3))
     (m-result (+ 3 6)))

  (let [m-plus-3-6 (m-plus (m-result 3) (m-result 6))
        m-plus-6-3 (m-plus (m-result 6) (m-result 3))]
    (= (m-bind (m-bind m-plus-3-6 mf) mg)
       (m-bind m-plus-6-3 (fn [x] (m-bind (mf x) mg))))))
