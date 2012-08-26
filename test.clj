;; this is a monadic function:
;; it takes a value and produces a monadic value
(defn half-double [x]
  (list (/ x 2) (* x 2)))

(def monadic-value (half-double 4))
;; monadic values () (4) (8 2 14)
monadic-value

;; this is a monadic function:
;; it takes a value and produces a monadic value
(defn increase [x]
  (list (inc x) (+ 2 x)))

;(mapcat increase value)

(def m-result list)
(defn m-bind [v f]
  mapcat f v)

;(m-bind (list 4) increase)
(m-bind (m-result 4) increase)

;; don't do this:
(def bad-fn 
  (compose [half-double increase]))
;; insead do following:
(def new-monadic-fn
  (m-chain [half-double increase]))

;; http://www.infoq.com/presentations/Monads-Made-Easy 19:31
