(ns zark.monads
  (:require [clojure.algo.monads :as m]))
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
;;   (m/domonad m/state-int-m
;;              [a g1
;;               b g2
;;               c g3]
;;              [a b c]))

;; (gs 5)
#_(m/domonad m/sequence-m
         [letters ['a 'b 'c]
          numbers [1 2 3]]
         [letters numbers])

(m/domonad m/identity-m [a 1
                         b (inc a)]
           (* a b))

(defn f [x]
  (m/domonad
   m/maybe-m ;; monad-name
   [a x
    b (inc a)] ;; monad-steps
   (* a b))) ;; monad-expression

(f 1)
