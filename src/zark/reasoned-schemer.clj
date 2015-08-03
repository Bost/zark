;;; -*- mode: clojure; mode: clojure-test -*-
(ns zark.reasoned-schemer
  (:use clojure.core.logic)
  (:refer-clojure :exclude [== inc reify >= <= > < =]))

;; the reasoned schemer

(run* [q]
  (== 1 1)) ;; (_0) - anything & nothing will solve this puzzle

(run* [q]
  u#)

;; (clojure.repl/doc def)
(def t# "just true" true)   ; (clojure.repl/doc t#)
(def f# "just false" false) ; (clojure.repl/doc f#)

;; '==' means 'unify'
(run* [q]
  (== f# q)) ;; (false) i.e successfull unification; it's value is f#

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

;; cons: Returns a new seq where x is the first element and seq is the rest.
(cons 1 ()) ;; create a list containg 1: (1)
;; lcons: Constructs a seq a with an improper tail d if d is a logic variable.
(lcons 1 ()) ;; create a list containg 1: (1)

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
      ;; (clojure.repl/doc cond)
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
     ;; (clojure.repl/doc ==)
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
     [(== x y) (fresh [x] (== z x)) (== f# x) s#]  ; 1. (y:false z:_0   x:false)
     [(fresh [x] (== y x)) (== z x) (== t# x) s#]  ; 2. (y:_0    z:true x:true)
     [s# u#]
     )
    (== (cons z (cons y ())) r))) ;; ((_0 false) (true _0))

(run* [r]
  (fresh [x y z]
    ;; (clojure.repl/doc conde)
    (conde
     [(== x y) (fresh [x] (== z x)) (== f# x) s#]  ; 1. (y:false z:_0   x:false)
     [(fresh [x] (== y x)) (== z x) (== t# x) s#]  ; 2. (y:_0    z:true x:true)
     [s# u#]
     )
    (== f# x) ; this colides with 2.
    (== (cons z (cons y ())) r))) ;; ((_0 false))

(run* [q]
  (let [a (== t# q)
        b (fresh [x]
            (== f# q)
            (== x q)
            )
        c (conde
           [(== t# q) s#]
           [s# (== f# q)])]
    a)) ;; (true) - b, c are ignored

(run* [q]
  (let [a (== t# q)
        b (fresh [x]
            (== f# q)
            (== x q)
            )
        c (conde
           [(== t# q) s#]
           [s# (== f# q)])]
    b)) ;; (false) - a, c are ignored

(run* [q]
  (let [a (== t# q)
        b (fresh [x]
            (== f# q)
            (== x q)
            )
        c (conde
           [(== t# q) s#]
           [s# (== f# q)])]
    c)) ;; (true false) - a, b are ignored


;; Chapter 2. Teaching Old Toys New Tricks

;; (clojure.repl/doc run*)

;; example nr 2.
(run* (r)
  (fresh [x y]
    (==
     (list x y) ; equivalent with (cons x (cons y ()))
     r))) ;; ((_0 _1)) the result in the book is (probably) wrong

(run* (r)
  (fresh [v w]
    (== (let [x v
              y w]
          (list x y))
        r))) ;; ((_0 _1))

(def car first)

;; (clojure.repl/doc firsto)
(car (list 'a 'c 'o 'r 'n)) ;; a
(car (list 1 2 3 4 5)) ;; 1

(run* [r]
  (firsto (list 'a 'c 'o 'r 'n) r)) ;; (a)

;; (clojure.repl/doc ==)
(run* [r]
  (fresh [x y z]
    (== x (lcons 1 (lcons 2 ())))
    (== y (list 1 2))
    (== z '(1 2))

    (== x y)
    (== y z)
    (== x z)

    (== t# r))) ;; (true) - the lists can be unified (are the same)

(run* (r)
  (fresh [x y]
    (firsto (list r y) x)
    (== 'pear x)))

(def caro
  "A function of 2 arguments"
  (fn [p a] ; in the a the result is accumulated
    (fresh [d]
      ;; (clojure.repl/doc lcons)
      (== (lcons a d) p))))

;; (run* [q]
;;   (fresh [x]
;;     (caro (list 1 2) x)
;;     (== t# q)))

(run* (r)
  (fresh [x y]
    (caro (list r y) x)
    (== 'pear x)))

;; page 26; the caro definition
