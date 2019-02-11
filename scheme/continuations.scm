;; -*- origami-fold-style: triple-braces -*-
;; start & set scheme repl: `M-x run-geiser' `M-x geiser-set-scheme'
;; folding: M-x origami-forward-toggle-node / z a

;; constant function fc: AnyType (ignored & ignored) -> Number
(define (fc) 1)

;; function of one argument fhw: String (infered) -> String
(define (fhw arg) (string-append "Hello World, " arg))

;; Motivational example {{{
;; [1] loop-fn is a compound-procedure of one argument `ls'
;; [2] If `ls' is '() then return the identity of `*', finish the current loop
;;     and proceed to the next elem of the `ls'
;; [3] If the first elem of `ls' is `0' then return the absorbing elem of `*',
;;     finish the current loop and proceed to the next elem of the `ls'.
;;     Inefficient :-(
;; [4] Multiply & loop around
;; See also http://www.cs.sfu.ca/CourseCentral/383/havens/notes/Lecture06.pdf"
(define (product init-ls)
  "Multiply the elements of a list. Inefficient"
  (let loop-fn ((ls init-ls))                       ;; [1]
    (cond
     ((null? ls) 1)                                 ;; [2]
     ((= (car ls) 0) 0)                             ;; [3]
     (else (* (car ls) (loop-fn (cdr ls)))))))      ;; [4]

(product '(1 2 3 4 5))     ;; => 120
(product '(7 3 8 0 1 9 5)) ;; => 0
(product '())              ;; => 1
;; }}}

;; Show inefficiency {{{
(define (to-str arg)
  "Helper function. Convert `arg' to string."
  (cond
   ((string? arg) arg)
   ((number? arg) (number->string arg))
   (else "to-str not implemented for this type")))

(define (product init-ls)
  "Inefficient & buggy"
  (string-append
   "The result is: "
   (to-str
    (let loop-fn ((ls init-ls))
      (cond
       ((null? ls) 1)
       ((= (car ls) 0) " a bloody 0. Haha!")
       (else (* (car ls) (loop-fn (cdr ls)))))))))
(product '(1 2 3 4 5))     ;; => "The result is: 120"
(product '())              ;; => "The result is: 1"
(product '(7 3 8 0 1 9 5)) ;; => Wrong type
;; }}}

;; A better way {{{
;; [1] Create continuation function `break' wrapping around the current
;;     computation - the `(let loop-fn ...)'
;; [2] loop-fn is a compound-procedure of one arg `ls'
;; [3] If `ls' is '() then return the identity of `*', finish the current loop
;;     and proceed to the next elem of the `ls'
;; [4] Continue with the rest of the computation
;;         `(string-append "The result is: " (to-string ...))'
;;     with the value of current computation being 0 thus effectivelly breaking
;;     out of the loop.
;; [5] Multiply & loop around
;; See also http://www.cs.sfu.ca/CourseCentral/383/havens/notes/Lecture06.pdf"
(define (product init-ls)
  "Multiply the elements of a list. Efficient but malevolent."
  (string-append
   "The result is: "
   (to-str
    (call/cc
     (lambda (break)                                         ;; [1]
       (let loop-fn ((ls init-ls))                      ;; [2]
         (cond
          ((null? ls) 1)                                ;; [3]
          ((= (car ls) 0) (break " a bloody 0. Haha!")) ;; [4]
          (else (* (car ls) (loop-fn (cdr ls))))))))))) ;; [5]

(product '(1 2 3 4 5))     ;; => "The result is: 120"
(product '())              ;; => "The result is: 1"
(product '(7 3 8 0 1 9 5)) ;; => "The result is: 0"
;; }}}

;; {{{
;; `call/cc' alias for `call-with-current-continuation'
;; https://www.gnu.org/software/guile/manual/html_node/Continuations.html
(call-with-current-continuation (lambda (k) 1))
(call/cc (lambda (k) 1))
;; }}}

(((call/cc
   (lambda (k) k))
  (lambda (x) x))
 "HEY!")

;; {{{
(call/cc
 (lambda (k)
   ;; (error (k "foo"))
   ;; the current computation `(/ 30 5 3)' is effectivelly ignored
   (/ 30 5 (k 1) 3)))
;; => 1
;; }}}

;; {{{
(define (foo return) (return 2) 3)
(define (baz arg)    (arg 5) 7)
(call/cc foo)   ;; => 2
(call/cc (foo)) ;; => error
(foo (lambda (x) x)) ;; => 3
(foo (lambda (x) 5)) ;; => 3
;; }}}

(define *x* '())  ;; global definition

(lambda (x)
  (set! *x* x)
  (+ 1 (x 2) 3))

(((lambda (x) x) 1) 2)

;; returns 3
(foo (lambda (x) x))
;; == ((foo (return) (return 2) 3) (lambda (x) x))
;; == ((lambda (x) x) 2) 3
;; == 2 3
;; == 3
(define *k* '())

(call/cc
 ;; k itself is the continuation
 ;; it represents (lambda (v) v)
 ;; and this time it's executed as: (+ 1 3)
 (lambda (k)
   (set! *k* k)
   (+ 1 3)))
;; => 4

(*k* (* 2 3)) ;; executed as ((lambda (v) v) (* 2 3))
;; => 6

(call/cc
 ;; k itself is the continuation
 ;; it represents (lambda (v) v)
 ;; and this time it's executed as: 2
 (lambda (k)
   (set! *k* k)
   (+ 1 (k 2) 3)))
;; => 2

(*k* (* 2 3)) ;; executed as ((lambda (v) v) (* 2 3))
;; => 6


;; `begin' is necessary every time you need several forms when the syntax allows only one form.
;; (begin f1 f2 ... fn) evaluates f1 ... fn in turn and then returns the value of fn.
;; `begin' is normally used when there is some side-effect e.g (begin (set! y (+ y 1)) y)


(+
 (call/cc
  ;; k itself is the continuation
  ;; it represents (lambda (v) (+ v 5))
  ;; and this time it's executed as: (+ (* 3 4) 5)
  (lambda (k)
    (begin
      (set! *k* k)
      (* 3 4))))
 5)
;; => 17

(*k* (* 2 3)) ;; executed as ((lambda (v) (+ v 5)) (* 2 3))
;; => 11

(+
 (call/cc
  ;; k itself is the continuation
  ;; it represents (lambda (v) (+ v 5))
  ;; and this time it's executed as: (+ (* 3 4) 5)
  (lambda (k)
    (begin
      (set! *k* k)
      (k (* 3 4)))))
 5)
;; => 17

(*k* (* 2 3)) ;; executed as ((lambda (v) (+ v 5)) (* 2 3))
;; => 11


(define (foo n)
  (* 2
     (call/cc
      ;; k itself is the continuation
      ;; it represents (lambda (v) (define (foo n) (* 2 v)))
      ;; this time it's executed as: (define (foo n) (* 2 (+ n 1)))
      (lambda (k)
        (begin
          (set! *k* k)
          (+ n 1))))))
;; => foo

(foo 5) ;; i.e. (* 2 (+ 5 1))
;; => 12
(*k* 5)
;; => 10


((lambda (v) (define (foo n) (* v 2))) 5)

continuation
(continuation 100)      ; => (foo (n) (* 100 2))
((continuation 100) 3)  ; => ((foo (n) (* 100 2)) 3) => 200

(lambda (n)
  200)

(define cont '())
(+ (call/cc
    (lambda (c)
      (begin
        (set! cont c)
        (c (* 3 4)))))
   5)

(cont 2)

(foo 3)

(define retry '())

(define factorial
  (lambda (x)
    (if (= x 0)
        (call/cc
         (lambda (k) (set! retry k) 1))
        (* x (factorial (- x 1))))))

(factorial 4)
(retry 1)
