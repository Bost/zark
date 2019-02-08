;; M-x run-geiser

(define (f) 1) ;; constant function f: Any -> 1

(define (foo)
  (display "hello\n")
  ;; (display (bar)) (newline)
  (exit))

(call-with-current-continuation (lambda (k) 1))

(((call-with-current-continuation
   (lambda (k)
     k))
  (lambda (x) x))
 "HEY!")

;; http://www.cs.sfu.ca/CourseCentral/383/havens/notes/Lecture06.pdf
;; product is a fn of one argument
;; [1] create continuation fn `break' wrapping around the current computation,
;;     the `(let ...)'
;; [2] f is a compound-procedure of one arg `ls'
;; [3] if `ls' is '() then return the identity of * and terminate
;; [4] continue with the rest of the computation
;;         `(string-append "The result is: " (number->string ...))'
;;     with the value of current computation being 0 thus effectivelly breaking
;;     out of the loop. Note: `(break "0 haha!")' gives incorrect type error.
;; [5] multiply & loop around
(define product
  (lambda (initial-ls)
    (string-append
     "The result is: "
     (number->string
      (call-with-current-continuation
       (lambda (break)                                          ;; [1]
         (let loop-fn ((ls initial-ls))                    ;; [2]
           (cond
            ((null? ls) 1)                                 ;; [3]
            ((= (car ls) 0) (break 0))                     ;; [4]
            (else (* (car ls) (loop-fn (cdr ls)))))))))))) ;; [5]

(product '(1 2 3 4 5))     ;; => "The result is: 120"
(product '(7 3 8 0 1 9 5)) ;; => "The result is: 0"
(product '())              ;; => "The result is: 1"

(call-with-current-continuation
 (lambda (k)
   ;; (error (k "foo"))
   ;; the current computation `(/ 30 5 3)' is effectivelly ignored
   (/ 30 5 (k 1) 3)))
;; => 1

(define (foo return)
  (return 2)
  3)

(define stuff (foo return)
  (return 2)
  3)

(foo (lambda (x) x))
;; => 3

(foo (lambda (x) 5))
;; => 3


(call-with-current-continuation foo)   ;; => 2
(call-with-current-continuation (foo)) ;; => error

(define *x* '())
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

(call-with-current-continuation
 ;; k itself is the continuation
 ;; it represents (lambda (v) v)
 ;; and this time it's executed as: (+ 1 3)
 (lambda (k)
   (set! *k* k)
   (+ 1 3)))
;; => 4

(*k* (* 2 3)) ;; executed as ((lambda (v) v) (* 2 3))
;; => 6

(call-with-current-continuation
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
 (call-with-current-continuation
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
 (call-with-current-continuation
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
     (call-with-current-continuation
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
(+ (call-with-current-continuation
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
        (call-with-current-continuation
         (lambda (k) (set! retry k) 1))
        (* x (factorial (- x 1))))))

(factorial 4)
(retry 1)
