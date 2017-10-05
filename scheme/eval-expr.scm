;; M-x run-scheme

;; (load "dmatch.scm") ;; this doesn't work
(load "pmatch.scm")

(define eval-expr
  (lambda (expr env)
    (pmatch expr
            [,n (guard (number? n))
                n]
            [(zero? ,e)
             (zero? (eval-expr e env))]
            [(add1 ,e)
             (add1 (eval-expr e env))]
            [(sub1 ,e)
             (sub1 (eval-expr e env))]
            [(* ,e1 ,e2)
             (* (eval-expr e1 env)
                (eval-expr e2 env))]
            [(if ,t ,c ,a)
             (if (eval-expr t env)
                 (eval-expr c env)
                 (eval-expr a env))]
            [,x (guard (symbol? x))
                (env x)]
            [(lambda (,x) ,body)
             (lambda (arg)
               (eval-expr body (lambda (y)
                                 (if (eq? x y)
                                     arg
                                     (env y)))))]
            [(,rator ,rand)
             ((eval-expr rator env)
              (eval-expr rand env))]
            )))

(printf
 "~s"
 ;; (eval-expr 'x (lambda (y) (error 'lookup "unbound")))
 ;; (eval-expr '(+ 1 2) (lambda (y) (error 'lookup "unbound")))
 ;; (eval-expr '(add1 (add1 2)) (lambda (y) (error 'lookup "unbound")))
 ;; (eval-expr '#t (lambda (y) (error 'lookup "unbound")))
 ;; (eval-expr '(if 3 1 2) (lambda (y) (error 'lookup "unbound")))
 ;; (eval-expr '(* 3 2) (lambda (y) (error 'lookup "unbound")))
 ;; (eval-expr '((lambda (x) x) 5) (lambda (y) (error 'lookup "unbound")))
 ;; (eval-expr '(((lambda (x) x) (lambda (y) y)) 5) (lambda (y) (error 'lookup "unbound")))
 (eval-expr
  '(((lambda (!)
       (lambda (n)
         ((! !) n)))
     (lambda (!)
       (lambda (n)
         (if (zero? n)
             1
             (* n ((! !) (sub1 n)))))))
    5)
  (lambda (y) (error 'lookup "unbound")))
 )
