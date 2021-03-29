(ns zark.monads
  (:require [clojure.algo.monads
             :refer
             [
              monad
              domonad with-monad
              sequence-m
              maybe-m
              identity-m
              writer-m
              m-lift
              ]]))

#_(comment
  (defn half-double "monadic fn: val -> monadic val" [x]
    (list (/ x 2) (* x 2)))

  (def monadic-value (half-double 4)) ;; monadic vals: (), (4), (8 2 14)

  (defn increase "monadic fn: val -> monadic val" [x]
    (list (inc x) (+ 2 x)))

  ;; (mapcat increase value)

  (def m-result list)
  (defn m-bind [v f]
    mapcat f v)

  ;; (m-bind (list 4) increase)
  (m-bind (m-result 4) increase)

  ;; don't do this:
  ;; (def bad-fn (compose [half-double increase]))

  ;; instead do following:
  ;; (def new-monadic-fn (m-chain [half-double increase]))

  ;; http://www.infoq.com/presentations/Monads-Made-Easy 19:31
  ;; list comprehension (for) 23:00
  ;; The video is also at https://www.youtube.com/watch?v=BHVu8VQJN7E

  (for [a '(11 2 3)
        b '(10 20 30)]
    (+ a b))

  ;; 29:00 - from here it's gonna be interesting

  ;; this function returns a basic value
  (defn basic-fn-a [x] x)
  ;; basic-fn-a [6]

  ;; this function returns a monadic value;
  ;; monadic value is a wrapper for a basic value
  (defn monadic-fn-a [x] [x])
  ;; monadic-fn-a [6]
  )

(defn g1 [state-int]
  [:g1 (inc state-int)])

(defn g2 [state-int]
  [:g2 (inc state-int)])

(defn g3 [state-int]
  [:g3 (inc state-int)])

;; (def gs
;;   (domonad m/state-int-m
;;              [a g1
;;               b g2
;;               c g3]
;;              [a b c]))

;; (gs 5)
#_(domonad m/sequence-m
         [letters ['a 'b 'c]
          numbers [1 2 3]]
         [letters numbers])

(domonad identity-m [a 1
                         b (inc a)]
           (* a b))

(defn f [x]
  (domonad
   maybe-m ;; monad-name
   [a x
    b (inc a)] ;; monad-steps
   (* a b))) ;; monad-expression

(f 1)

(defprotocol ltime-monad-protocol
  "Accumulation of values into containers"
  (ltime-m-combine [container1 container2]
  "combine two containers, return new container"))

(extend-protocol ltime-monad-protocol
  clojure.lang.PersistentArrayMap
  (ltime-m-combine [c1 c2]
    ((comp
      (fn [c2]
        (update-in c2 [:idx] (fn [v]
                               (+ v (:idx c1))
                               #_(inc (or (:idx c1) 0)))))
      (fn [c2]
        (update-in c2 [:total] (fn [v]
                                 #_v
                                 (+ v (:total c1))))))
     c2)))

(defn ltime-m
  "Monad describing computations that accumulate data on the side, e.g. for
   logging. The monadic values have the structure [value log]."
  []
  (monad
   [m-result  (fn m-result-ltime [v]
                [v {:b 0 :e 0 :total 0 :idx 0}])
    m-bind
    (fn m-bind-ltime [mv f]
      (let [[v1 a1] mv
            [[v2 a2] acc] (let [tbeg
                                0
                                #_(System/currentTimeMillis)]
                            (let [v (f v1)] ;; v is a monadic value already
                              (let [tend
                                    0
                                    #_(System/currentTimeMillis)]
                                [v {:b tbeg :e tend :total (- tend tbeg)}])))
            ]
        [v2 (ltime-m-combine a1 a2)]))]))


(defn m-result [v]
  [v {:b 0 :e 0 :total 0 :idx 0}])

(defn m-inc [x]
  (let [[v m] (m-result (inc x))]
    [v (update-in m [:idx]
                  inc
                  #_(fn [_] 0))]))

(defn ltime-m-combine [c1 c2]
  ((comp
    (fn [c2]
      (update-in c2 [:idx] (fn [v] (+ v (:idx c1)))))
    (fn [c2]
      (update-in c2 [:total] (fn [v] (+ v (:total c1))))))
   c2))

(defn m-bind [mv f]
  (let [
        [v1 a1] mv
        [[v2 a2] acc] (let [tbeg
                            0
                            #_(System/currentTimeMillis)]
                        (let [v (f v1)] ;; v is a monadic value already
                          (let [tend
                                0
                                #_(System/currentTimeMillis)]
                            [v {:b tbeg :e tend :total (- tend tbeg)}])))
        ]
    [v2 (ltime-m-combine a1 a2)]))

;; 1. monadic law:
(= (m-bind (m-result 1) m-inc)
   (m-inc 1))

(domonad (ltime-m)
         [x (m-result 1)]
         (inc x))

;; 2. monadic law:
(= (m-bind (m-result 1) m-result)
   (m-result 1))

(= (domonad (ltime-m)
            [x (m-result 1)]
            x)
   (m-result 1))

;; 3. monadic law doesn't hold cause (System/currentTimeMillis) is different for
;; every invocation
(= (m-bind (m-bind (m-result 1) m-inc)
           m-inc)
   (m-bind (m-result 1)
           (fn [x] (m-bind (m-inc x) m-inc))
           )
   )

(= (domonad (ltime-m)
            [y (domonad (ltime-m)
                        [x (m-result 1)]
                        (inc x))]
            (inc y))
   (domonad (ltime-m)
            [x (m-result 1)
             y (m-result (inc x))]
            (inc y))
   )
