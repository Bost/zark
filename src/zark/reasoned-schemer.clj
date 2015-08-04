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
(def cdr rest)

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

(cons
 (car (list 'grape 'raisin 'pear))
 (car (list (list 'a) (list 'b) (list 'c)))) ;; (grape (a) (b) (c)))

(run* [r]
  (fresh [x y]
    (caro #_firsto (list 'grape 'raisin 'pear) x) ;; x: grape
    (caro #_firsto (list (list 'a) (list 'b) (list 'c)) y) ;; y: (list a)
    (== (lcons x y) r))) ;; ((grape a))

;; (clojure.repl/doc rest)
(cdr (list 'grape 'raisin 'pear))

(def cdro
  "A function of 2 arguments - almost own deduction! Yay! :)"
  (fn [p a]
    (fresh [d]
      (== (lcons d a) p))))

(run* [r]
  (fresh [v]
    (cdro (list 'a 'c 'o 'r 'n) v)
    (caro v r)))

(run* [a]
  ;; (clojure.repl/doc cdro)
  (cdro (list 'grape 'raisin 'pear) a))

(cons
 (cdr (list 'grape 'raisin 'pear))
 (car (list (list 'a) (list 'b) (list 'c))))
;; (cons (list 'raisin 'pear) (list 'a)) => ((raisin pear) a)

(run* [r]
  (fresh [x y]
    (cdro (list 'a 'c 'o 'r 'n) x)                ; x: (list 'c 'o 'r 'n)
    (caro (list (list 'a) (list 'b) (list 'c)) y) ; y: (list 'a)
    (== (lcons x y) r))) ; (lcons (list 'c 'o 'r 'n) (list 'a)) => ((c o r n) a) => (((c o r n) a))

(run* [q]
  (cdro (list 'a 'c 'o 'r 'n) (list 'c 'o 'r 'n))
  (== t# q))

(run* [l]
  (fresh [x]
    ;; it becomes clear if I change the order of expressions:
    (cdro l (list 'c 'o 'r 'n))
    (caro l x)
    (== 'a x)))

(run* [l]
  (fresh [x]
    ;; it becomes clear if I change the order of expressions:
    (== 'a x)
    (caro l x)                   ; (caro _0 'a)
    (cdro l (list 'c 'o 'r 'n))  ; (cdro _0 (list 'c 'o 'r 'n))
    ))
;;caro and cdro are complementary - so probably I take take the lcons because ???
;; (it belongs to the pack)

(run* [l]
  (conso (list 'a 'b 'c) (list 'd 'e) l)) ;; (((a b c) d e))

(run* [x]
  (conso x (list 'a 'b 'c) (list 'd 'a 'b 'c))) ;; (d)

(run* [r]
  (fresh [x y z]
    (== (list 'e 'a 'd x) r)    ; r: (list 'e 'a 'd _0)
    (conso y (list 'a z 'c) r))) ; (conso _1 (list 'a _2 'c) (list 'e 'a 'd _0))
;; x: 'c
;; r: ((list 'e 'a 'd 'c))
;; z: 'd
;; y: 'e

(run* [x]
  (conso x (list 'a x 'c) (list 'd 'a x 'c)));; d

;; page 29

(run* [l]
  (fresh [x]
    (== (list 'd 'a x 'c) l)      ; l: (list 'd 'a x 'c)
    (conso x (list 'a x 'c) l)))
;; (conso x (list 'a x 'c) (list 'd 'a x 'c)) => ((list 'd 'a 'd 'c))

(def -cons0
  (fn [a b c]
    (== (lcons a b) c)))

(def +cons0
  (fn [a b c]
    (caro c a)
    (cdro c b)
    (== (lcons a b) c)))

(run* [x]
  (-cons0 1 (list 2) x))

(run* [x]
  (+cons0 1 (list 2) x))

(run* [x]
  (-cons0 'a (list 'b) (list 'a 'b))
  (== t# x))

(run* [x]
  (+cons0 'a (list 'b) (list 'a 'b))
  (== t# x))

(run* [x]
  (fresh [u v]
    (-cons0 'a (list 'b) u)
    (+cons0 'a (list 'b) v)
    (== u v)
    (== t# x)))

(run* [x]
  (fresh [a b u v]
    (-cons0 a (list b) u)
    (+cons0 a (list b) v)
    (== u v)
    (== t# x)))

(run* [x]
  (fresh [a b]
    (-cons0 a (list b) (list a b))
    (+cons0 a (list b) (list a b))
    (== t# x)))

;; page 29
