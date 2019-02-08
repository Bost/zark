;; M-x run-scheme then copy & paste the sexps to the repl

;; Under Spacemacs:
;; 1. guile - The GNU Project Extension Language
;;    GNU Guile is an implementation of the Scheme programming language
;;
;;    guile-2.0 won't work. Install guile-2.2:
;;        $ sudo apt-get install guile-2.2
;; 2. add `scheme' to `dotspacemacs-configuration-layers'
;; 3. M-x run-geiser

(define a-friend
  (lambda (x y)
    (null? y)))

(a-friend 1 1)  ;; => #f
(a-friend 1 ()) ;; => #t

;; a: atom
;; lat: list of atoms
;; col: collector / continuation
;; (car '(1 2 3)): 1
;; (cdr '(1 2 3)): (2 3)
;; (cons 1 (cons 2 (cons 3 nil))) == (list 1 2 3) == (1 . (2 . (3 . nil)))
;; continuation example on p.137 of The Little Schemer
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
      ((< 3 2) 'less)) ;; => greater

(false? false) ;; => #t

(cond (false 'greater)
      (false 'less)) ;; => Unspecified return value

;; `let' used for looping; similar to a named function, but it’s ephemeral.
;; `my-loop' won't leak out of context of the `let'
;; https://blog.veitheller.de/Scheme_Macros_III:_Defining_let.html
(let my-loop ((x 1))
  (if (> x 10)
      (write "We’re done!")
      (my-loop (+ x 1))))
