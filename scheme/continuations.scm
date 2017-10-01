;; M-x run-scheme

(define a-friend
  (lambda (x y)
    (null? y)))

;; a: atom
;; lat: list of atoms
;; col: collector / continuation
;; (car '(1 2 3)): 1
;; (cdr '(1 2 3)): (2 3)
;; (cons 1 (cons 2 (cons 3 nil))) == (list 1 2 3) == (1 . (2 . (3 . nil)))
(define multirember-and-co
  (lambda (a lat col)
    (cond
     ((null? lat)
      (col '() '()))
     ((eq? (car lat) a)
      (multirember-and-co a
                          (cdr lat)
                          (lambda (newlat seen)
                            (col newlat
                                 (cons (car lat) seen)))))
     (else
      (multirember-and-co a
                          (cdr lat)
                          (lambda (newlat seen)
                            (col (cons (car lat) newlat)
                                 seen)))))))

;; (multirember-and-co a lat col)
(multirember-and-co "tuna" () a-friend)
(multirember-and-co "tuna" '("tuna") a-friend)

(cond ((> 3 2) 'greater)
      ((< 3 2) 'less))

(cond (false 'greater)
      (false 'less))

(((call-with-current-continuation
   (lambda (k)
     k))
  (lambda (x) x))
 "HEY!")

(define product
  (lambda (ls)
    (call-with-current-continuation
      (lambda (break)
        (let f ((ls ls))
          (cond
            ((null? ls) 1)
            ((= (car ls) 0) (break 1))
            (else (* (car ls) (f (cdr ls))))))))))

(product '(1 2 3 4 5))
(product '(7 3 8 0 1 9 5))

(let f (('(1 2) '(1 2)))
  (display f))

;; (error "jim")

(call-with-current-continuation
 (lambda (k)
   ;; (error (k "foo"))
   (/ 5 (k 1))
   ))

(define *k* '())
(call-with-current-continuation
 (lambda (k)
   (set! *k* k)
   (+ 1 (k 2) 3)))

(define *x* '())
(lambda (x)
  (set! *x* x)
  (+ 1 (x 2) 3))

*x*
(+ 4 (*x* 1) 5)

(call/cc
  (lambda (k)
    (+ 1 (k 2))))

*k*
(+ 4 (*k* 1) 5)

((lambda (k) k) 3)
(/ 5 ((lambda (k) k) 3))

(/ 5 (*k* 3))


(define (foo return)
  (return 2)
  3)

(((lambda (x) x) 1) 2)

;; returns 3
(foo (lambda (x) x))
;; == ((foo (return) (return 2) 3) (lambda (x) x))
;; == ((lambda (x) x) 2) 3
;; == 2 3
;; == 3

;; returns 2
(call-with-current-continuation foo)
;; == (call-with-current-continuation (foo (return) (return 2) 3))

(call-with-current-continuation
 (lambda (k)
   (k 2)
   3
   )
 )


(define *k* '())
(+
 (call-with-current-continuation
  ;; k itself is the continuation
  ;; it represents (lambda (v) (+ v 5))
  (lambda (k)
    (begin
      (set! *k* k)
      (k (* 3 4)))))
 5)

(*k* (* 3 4))

(lambda (v)
   (+ (* v 4) 5))

(let ((v 3))
  (+ (* v 4) 5))

((lambda (v)
   (+ (* v 4) 5))
 4)

(+ 1 2)

(lambda (v) v)
5

(define continuation '())
(define (foo n)
  (* (call-with-current-continuation
      (lambda (c)
        (begin
          (set! continuation c)
          (+ n 1))))
     2))

(foo 20)
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
