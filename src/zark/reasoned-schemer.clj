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
    (== x q)))

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
     [(teacupo x) (== t# y) s#]   ;; x is (tea cup); y is true; whole result is ((tea true) (cup true))
     [(== f# x) (== t# y)]        ;; x is false; y is true; whole result is (false true)
     [s# u#])
    (== (cons x (cons y ())) r))) ;; ((tea true) (cup true) (false true))

(run* [r]
  (fresh [x y z]
    (conde
     [(== x y) (fresh [x] (== z x))]  ; (_0 _1)
     [(fresh [x] (== y x)) (== z x)]  ; (_0 _1)
     [s# u#]
     )
    (== (cons y (cons z ())) r))) ;; ((_0 _1) (_0 _1))

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
            (== x q))
        c (conde
           [(== t# q) s#]
           [s# (== f# q)])]
    a)) ;; (true) - b, c are ignored

(run* [q]
  (let [a (== t# q)
        b (fresh [x]
            (== f# q)
            (== x q))
        c (conde
           [(== t# q) s#]
           [s# (== f# q)])]
    b)) ;; (false) - a, c are ignored

(run* [q]
  (let [a (== t# q)
        b (fresh [x]
            (== f# q)
            (== x q))
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
(car '(a c o r n))          ;; a
(car (list 1 2 3 4 5)) ;; 1

(run* [r]
  (firsto (list 'a 'c 'o 'r 'n) r)) ;; (a)
(run* [r]
  (firsto '(a c o r n) r))          ;; (a)

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

(car '((a) (b) (c))) ;; (a)

(cons
 (car (list 'grape 'raisin 'pear))
 (car (list (list (list 'a) (list 'b) (list 'c))))) ;; (grape (a) (b) (c)))

(cons
 (car '(grape raisin pear))
 (car '(((a) (b) (c)))))      ;; (grape (a) (b) (c)))

(run* [r]
  (fresh [x y]
    (caro #_firsto (list 'grape 'raisin 'pear) x) ;; x: grape
    (caro #_firsto (list (list 'a) (list 'b) (list 'c)) y) ;; y: (list a)
    (== (lcons x y) r))) ;; ((grape a))

;; (clojure.repl/doc rest)
(cdr (list 'grape 'raisin 'pear)) ;; (rainin pear)
(cdr '(grape raisin pear))        ;; (rainin pear)

(def cdro
  "A function of 2 arguments - almost own deduction! Yay! :)"
  (fn [p a]
    (fresh [d]
      (== (lcons d a) p))))

(run* [r]
  (fresh [v]
    (cdro '(a c o r n) v)
    (caro v r))) ;; (c)

(run* [a]
  ;; (clojure.repl/doc cdro)
  (cdro '(grape raisin pear) a))

(cons
 (cdr '(grape raisin pear))
 (car '((a) (b) (c))))
;; (cons '(raisin pear) (a)) => ((raisin pear) a)

(run* [r]
  (fresh [x y]
    (cdro '(a c o r n) x)                ; x: '(a c o r n)
    (caro '((a) (b) (c)) y) ; y: '(a)
    (== (lcons x y) r))) ; (lcons '(a c o r n) '(a)) => ((c o r n) a) => (((c o r n) a))

(run* [q]
  (cdro '(a c o r n) '(c o r n))
  (== t# q)) ;; (true)

(run* [l]
  (fresh [x]
    ;; it becomes clear if I change the order of expressions:
    (cdro l '(c o r n))
    (caro l x)
    (== 'a x))) ;; ((a c o r n))

(run* [l]
  (fresh [x]
    ;; it becomes clear if I change the order of expressions:
    (== 'a x)
    (caro l x)                   ; (caro _0 'a)
    (cdro l '(c o r n))))  ; (cdro _0 '(c o r n))
;;caro and cdro are complementary - so probably I take take the lcons because ???
;; (it belongs to the pack)

(run* [l]
  (conso '(a b c) '(d e) l)) ;; (((a b c) d e))

(run* [x]
  (conso x '(a b c) '(d a b c))) ;; (d)

(run* [r]
  (fresh [x y z]
    ;; can't use the '(e a d x) - x is a free variable
    (== (list 'e 'a 'd x) r)      ;; r: '(e a d _0)
    (conso y (list 'a z 'c) r))) ;; (conso _1 '(a _2 c) '(e a d _0))

(run* [r]
  (fresh [x y z]
    (== (list 'e 'a 'd x) r)    ; r: (list 'e 'a 'd _0)
    (conso y (list 'a z 'c) r))) ; (conso _1 (list 'a _2 'c) (list 'e 'a 'd _0))
;; x: 'c
;; r: ((e a d c))
;; z: 'd
;; y: 'e

(run* [x]
  (conso x (list 'a x 'c) (list 'd 'a x 'c))) ;; (d)

;; page 29

(run* [l]
  (fresh [x]
    (== (list 'd 'a x 'c) l)      ; l: (list 'd 'a x 'c)
    (conso x (list 'a x 'c) l)))
;; (conso x (list 'a x 'c) (list 'd 'a x 'c)) => ((d a d c))

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
  (-cons0 'a '(b) '(a b))
  (== t# x)) ;; (true)

(run* [x]
  (+cons0 'a '(b) '(a b))
  (== t# x)) ;; (true)

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

(run* [l]
  (fresh [d x y w s]
    (conso w (list 'a 'n 's) s) ;; 1. (conso w (list 'a 'n 's) s) => s = (list _ 'a 'n 's)
    (cdro l s)                  ;; 2. (cdro l (list _ 'a 'n 's))  => l = (list _ _'a 'n 's)
    (caro l x)                  ;; 4. (caro (list _ _ 'a 'n 's) 'b)  => l = (list 'b _ 'a 'n 's)
    (== 'b x)                   ;; 3.
    (cdro l d)                  ;; 7. (cdro (list 'b _ 'a 'n 's) (list 'e _)) => l = (list 'b 'e 'a 'n 's)
    (caro d y)                  ;; 6. (caro d 'e) => d = (list 'e _)
    (== 'e y)))                 ;; 5.

(empty? (list 'grape 'raising 'pear)) ;; false
(empty? '(grape raising pear))        ;; false

(empty? (list)) ;; true
(empty? '())    ;; true

(run* (q)
  (emptyo '(grape raisin pear))
  (== t# q)) ;; ()

;; page 30

(clojure.core/= 'pear 'plum)
(clojure.core/= 'plum 'plum)

(run* (q)
      (== 'plum 'plum)
      (== t# q)) ;; (true)

(def eqo
  (fn [x y]
    (== x y)))

(defn pair? [x]
  (if (or (lcons? x)
          (and (coll? x) (seq x)))
    true false))

;; llist - Constructs a seq from 2 or more args. Last argument is tail
;; list - Creates a new list containing the items.

(list 'pear)                        ;; (pear)
;; (llist 'pear)                    ;; exception - must be at least 2 args
(list 'split 'pear)                 ;; (split pear)
(llist 'split 'pear)                ;; (split . pear)
(llist 'split '())                  ;; (split)
(llist 'split (list 'pear))         ;; (split pear)
(llist 'split (llist 'pear 'plum))  ;; (split pear . plum)

(pair? (list 'split 'pear))         ;; true
(pair? (llist 'split 'pear))        ;; true
(pair? (list (llist 'split 'pear))) ;; true
(pair? (list 'pear))                ;; true

(list)                              ;; ()
(pair? (list))                      ;; false
'()                                 ;; ()
(pair? '())                         ;; false
(pair? 'pear)                       ;; false
(pair? (list 'pear))                ;; true
(pair? (list 'a 'b))                ;; true
(pair? (list 'a 'b 'c))             ;; true

(cdr (list 'split))                 ;; ()

;; (cons (list 'split) 'pea)        ;; exception
(cons 'pea (list 'split))           ;; (pea split)
(lcons (list 'split) 'pea)          ;; ((split) . pea)

(def pairo
  (fn [p]
    (fresh [a d]
      (conso a d p))))

(run* [q]
  (pairo (cons q q))
  (== t# q)) ;; (true)

;; (pairo '())    ;; returns an object
;; (pairo (list)) ;; returns an object
;; (pairo ())     ;; returns an object

(run* [q]
  (pairo (list))
  (== t# q)) ;; ()

(run* (q)
      (pairo 'pair)
      (== t# q)) ;; ()

;; Chapter 3. Seeing old friend in new ways; page 35

(seq? nil)    ;; false
(seq? 'a)     ;; false
(seq? (list)) ;; true
(seq? '())    ;; true

(seq? '((a) (a b) c))   ;; true

(clojure.core/= '() (list))          ;; true
(clojure.core/= '(()) (list (list))) ;; true

(clojure.core/= '((a) (a b) c)
                (list '(a) '(a b) 'c)
                (list (list 'a) (list 'a 'b) 'c)) ;; true

(seq? '((a) (a b) c)) ;; true

;; following two s-expressions seem to be equal
'(a . b)      ;; (a . b)
(llist 'a 'b) ;; (a . b)
;; but(!) they are not:
(clojure.core/= '(a . b)
                (llist 'a 'b)) ;; false - the types are different:
(type '(a . b))      ;; PersistentList
(type (llist 'a 'b)) ;; LCons

(def list?
  (fn [l]
    (cond
      ;; (empty? nil) ;; true
      ;; (empty? ())  ;; true
      (empty? l)
      t#

      ;; (pair? '(a b)) ;; true
      (pair? l)
      (if (list? (cdr l)) t# f#)

      :else f#
      )))

(type (cdr '(a b))) ;; PersistentList
(type (seq '(a b))) ;; PersistentList

(cdr nil) ;; ()
(pair? ()) ;; ()

(list? '(d a t e s)) ;; true
(seq? '(d a t e s))  ;; true

(run* [r]
  (cdro '(a) r))

(def listo
  (fn [l]
    (conde
     [(emptyo l) s#]
     [(pairo l) (fresh [d]    ;; unnesting - the s-expression (fresh ...)
                  (cdro l d)  ;; in the end cdro returns an empty list
                  (listo d))]
     [s# u#]
     )))

;; page 36

(run* [r]
  (listo (llist 'a 'b r 'c))) ;; (_0)

(run 1 [r]
  (listo (list 'a 'b 'c . x))) ;;
