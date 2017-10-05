;;; -*- mode: clojure; mode: clojure-test -*-
(ns ^{:doc "
The Little Schemer

The Seasoned Schemer
https://doc.lagout.org/programmation/Lisp/Scheme/The%20Seasoned%20Schemer%20-%20Daniel%20P.%20Friedman.pdf

The Reasoned Schemer

"}
    schemer.core
  (:require [clojure.core.logic.fd :as fd])
  (:use clojure.core.logic)
  (:refer-clojure :exclude [== inc reify])
  (:gen-class))

(run* [q]
  u#)

(def t# true)
(def f# false)

;; '==' means 'unify'
(run* [q]
  (== t# q))

(run* [q]
  u#
  (== t# q))

(run* [q]
  s#
  (== t# q))

(run* [q]
  s#
  (== 'a 'a)
  (== 'corn q))

(run* [q]
  s#
  (== f# q))

;; _0 represents a fresh variable
(run* [x]
  (let [x t#]
    (== t# x)))

(run* [q]
  (fresh [x]
    (== t# x)
    (== t# q)))

(run* [q]
  (fresh [x]
    (== f# x)
    (== t# q)))

(run* [q]
  s#)

(run* [x]
  (let [x f#]
    (fresh [x]
      (== t# x))))

(cons 1 ()) ;; create a list containg 1: (1)

;; (cdr '(1 2))

(run* [r]
  (fresh [x y]
    (== (cons x
              (cons y ()) ;; (y)
              )           ;; (x y)
        r)))              ;; ((_0 _1))

(run* [r]
  (fresh [x]     ;; x0 is _0
    (let [y x]   ;; y is x0 and that is _0
      (fresh [x] ;; x1 is _1
        (== (cons x             ;; x1 because x0 is not visible
                  (cons y       ;; x0
                        (cons x ;; x1
                              ()))) r)))))
;; the value of the expression above is ((_1 _0 _1)) and that is the same as ((_0 _1 _0))

(run* [q]
  (fresh [x]
    (== t# x)
    (== x q)
    ))

(run* [q]
  (== q
      (cond
        f# t#
        :else f#)))

(run* [q]
  (== q
      (cond
        t# s#
        :else u#)))

(run* [q]
  (conde
   [u# s#]
   [s# u#]
   ))

(run* [q]
  (conde
   [u# u#]
   [s# s#]
   ))

(run* [q]
  (conde
   [s# s#]
   [s# u#]
   ))

(run* [x]
  (conde
   [(== 'olive x) s#]  ;; for every clause [..] x get refreshed
   [(== 'oil x) s#]
   [s# u#]
   ))

;; execute only first 2 clauses
(run 2 [x]
  (conde
   [(== 'olive x) s#]  ;; for every clause [..] x get refreshed
   [(== 'gin x) s#]
   [(== 'oil x) s#]
   [s# u#]
   ))

(run* [x]
  (conde
   [(== 'olive x) u#]  ;; for every clause [..] x get refreshed
   [(== 'gin x) s#]
   [s# s#]
   [(== 'oil x) s#]
   [s# u#]
   ))

(run* [r]
  (fresh [x y]
     (== 'olive x)
     (== 'oil y)
     (== (cons x (cons y ())) r)
     ))

(run* [r]
  (fresh [x y]
    (conde
     [(== 'split x) (== 'pea y) s#]
     [(== 'navy x) (== 'bean y)]
     [s# u#]
     )
    (== (cons x (cons y (cons 'aaa ()) )) r)
    ))

(run* [x]
  (conde
   [(== 'tea x) s#]
   [(== 'cup x) s#]
   [s# u#]
   ))

(def teacupo
  (fn [x]
    (conde
     [(== 'tea x) s#]
     [(== 'cup x) s#]
     [s# u#]
     )))

(run* [x]
  (teacupo x)) ;; => x is (tea cup)

(run* [r]
  (fresh [x y]
    (conde
     [(teacupo x) (== t# y) s#]  ;; x is (tea cup); y is true; whole result is ((tea true) (cup true))
     [(== f# x) (== t# y)]       ;; x is false; y is true; whole result is (false true)
     [s# u#])
    (== (cons x (cons y ())) r)
    )) ;; ((tea true) (cup true) (false true))

(run* [r]
  (fresh [x y z]
    (conde
     [(== x y) (fresh [x] (== z x))]  ; (_0 _1)
     [(fresh [x] (== y x)) (== z x)]  ; (_0 _1)
     [s# u#]
     )
    (== (cons y (cons z ())) r)
    )) ;; ((_0 _1) (_0 _1))

(run* [r]
  (fresh [x y z]
    (conde
     [(== x y) (fresh [x] (== z x)) (== f# x) s#]  ; (_0 _1)
     [(fresh [x] (== y x)) (== z x) (== t# x) s#]  ; (_0 _1)
     [s# u#]
     )
    ;; (== f# x)
    (== (cons z (cons y ())) r)
    ))

(defn cinema-1 [n]
  (+ 40 (* (- n 1) 4)))

(defn cinema [n]
  (for [i (range n)]
    (->> i
         clojure.core/inc
         cinema-1)))

(run* [n]
  s#
  (== 1224
      n
      #_(cinema n)))

(run* [q]
  (fd/in q (fd/interval 1 5)))

(run* [q]
  (fresh [x y]
    (fd/in x y (fd/interval 1 10))
    (fd/+ x y 10)
    (== q [x y])))
