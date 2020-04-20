;; -*- origami-fold-style: triple-braces -*-
;; start & set scheme REPL: `M-x run-geiser' `M-x geiser-set-scheme'
;; folding: M-x origami-forward-toggle-node / z a

;; Quick recap on LISP {{{
;; L-I-S t   P-rocessing
;; constant function fc: AnyType (ignored & ignored) -> Number
(define (fc) 1)

;; function of one argument fhw: String (infered) -> String
(define (fhw arg) (string-append "Hello World, " arg))
;; }}}

;; Motivational example {{{
;; [1] loop-fn is a compound-procedure of one argument `ls'
;; [2] If `ls' is '() then return the identity of `*', finish the current loop
;;     and proceed to the next elem of the `ls'
;; [3] If the first elem of `ls' is `0' then return the absorbing elem of `*',
;;     finish the current loop and proceed to the next elem of the `ls'.
;;     Inefficient :-(
;; [4] Multiply & loop around
;; See also http://www.cs.sfu.ca/CourseCentral/383/havens/notes/Lecture06.pdf"
(define (multiply init-ls)
  "Multiply the elements of a list. Inefficient"
  (let loop-fn ((ls init-ls))                       ;; [1]
    (cond
     ((null? ls) 1)                                 ;; [2]
     ((= (car ls) 0) 0)                             ;; [3]
     (else (* (car ls) (loop-fn (cdr ls)))))))      ;; [4]

(multiply '(1 2 3 4 5))     ;; => 120
(multiply '(7 3 8 0 1 9 5)) ;; => 0
(multiply '())              ;; => 1
;; }}}

;; Show inefficiency {{{
(define (to-str arg)
  "Helper function. Convert `arg' to string."
  (cond
   ((string? arg) arg)
   ((number? arg) (number->string arg))
   (else "to-str not implemented for this type")))

(define (multiply init-ls)
  "Inefficient & buggy"
  (string-append
   "The result is: "
   (to-str
    (let loop-fn ((ls init-ls))
      (cond
       ((null? ls) 1)
       ((= (car ls) 0) " a bloody 0. Haha!")
       (else (* (car ls) (loop-fn (cdr ls)))))))))
(multiply '(1 2 3 4 5))     ;; => "The result is: 120"
(multiply '())              ;; => "The result is: 1"
(multiply '(7 3 8 0 1 9 5)) ;; => Wrong type
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
(define (multiply init-ls)
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

(multiply '(1 2 3 4 5))     ;; => "The result is: 120"
(multiply '())              ;; => "The result is: 1"
(multiply '(7 3 8 0 1 9 5)) ;; => "The result is: 0"
;; }}}

;; {{{
;; `call/cc' alias for `call-with-current-continuation'
;; https://www.gnu.org/software/guile/manual/html_node/Continuations.html
(call-with-current-continuation (lambda (k) 1))
(call/cc (lambda (k) 1))
;; }}}

;; Examples {{{
;; See also https://youtu.be/Ju3KKu_mthg?t=1167
(call/cc  ;; call/cc saves the current stack state `s0' into the variable `k'
 (lambda (k)   ;; `lambda' creates the next stack `s1' on top of `s0'.
   ;; (error (k "foo"))
   (/ 30
      5
      ;; (k 1) restores the stack state `s0' and passes it `1' as if `1' were
      ;; computed by the stack frame `s1', i.e. the current computation of the
      ;; stack frame `s1: (/ 30 5 3)' is effectively ignored.
      (k 1)
      3)))
;; => 1

(((call/cc
   (lambda (k) k))
  (lambda (x) x))
 "HEY!")
;; }}}

;; {{{
;; call/cc creates "aborting" continuations that ignore the rest of the
;; computation inside the body of the (lambda (k) ...) when k is invoked. See
;; delimited continuations
(define (foo return) (return 2) 3)
(define (baz arg)    (arg 5) 7)
(call/cc foo)   ;; => 2
(call/cc (foo)) ;; => error
(foo (lambda (x) x)) ;; => 3
(foo (lambda (x) 5)) ;; => 3
;; }}}

;; Chicken Scheme {{{
;; Jonathan Bartlett: Continuations: The Swiss Army Knife of Flow Control
;; https://youtu.be/Ju3KKu_mthg

;; Ambiguous value operator: `amb'
;; Yields one of those values, depending on which satisfies future constraints
;; 28:55
(import amb)
(amb 1 2 3 4 5)
;; Create assertions that have to be true. These do not have to directly involve
;; the variables
(amb-assert (> a b))
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
