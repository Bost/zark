(ns zark.monads
  (:require [clojure.algo.monads
             :refer
             [
              monad domonad with-monad
              sequence-m
              maybe-m
              identity-m
              writer-m
              m-lift
              state-m
              reader-m
              set-m
              cont-m
              ]]
            [clojure.algo.generic.arithmetic :as alg]))

#_(comment
  (defn half-double "monadic fn: val -> monadic val" [x]
    (list (/ x 2) (* x 2)))

  (def monadic-value (half-double 4)) ;; monadic vals: (), (4), (8 2 14)

  (defn increase "monadic fn: val -> monadic val" [x]
    (list (inc x) (+ 2 x)))

  ;; (mapcat increase value)

  (def m-result list)
  (defn m-bind [v f]
    mapcat f v)

  ;; (m-bind (list 4) increase)
  (m-bind (m-result 4) increase)

  ;; don't do this:
  ;; (def bad-fn (compose [half-double increase]))

  ;; instead do following:
  ;; (def new-monadic-fn (m-chain [half-double increase]))

  ;; http://www.infoq.com/presentations/Monads-Made-Easy 19:31
  ;; list comprehension (for) 23:00
  ;; The video is also at https://www.youtube.com/watch?v=BHVu8VQJN7E

  (for [a '(11 2 3)
        b '(10 20 30)]
    (+ a b))

  ;; 29:00 - from here it's gonna be interesting

  ;; this function returns a basic value
  (defn basic-fn-a [x] x)
  ;; basic-fn-a [6]

  ;; this function returns a monadic value;
  ;; monadic value is a wrapper for a basic value
  (defn monadic-fn-a [x] [x])
  ;; monadic-fn-a [6]
  )

(defn g1 [state-int]
  [:g1 (inc state-int)])

(defn g2 [state-int]
  [:g2 (inc state-int)])

(defn g3 [state-int]
  [:g3 (inc state-int)])

;; (def gs
;;   (domonad m/state-int-m
;;              [a g1
;;               b g2
;;               c g3]
;;              [a b c]))

;; (gs 5)
#_(domonad m/sequence-m
         [letters ['a 'b 'c]
          numbers [1 2 3]]
         [letters numbers])

(domonad identity-m [a 1
                         b (inc a)]
           (* a b))

(defn f [x]
  (domonad
   maybe-m ;; monad-name
   [a x
    b (inc a)] ;; monad-steps
   (* a b))) ;; monad-expression

(f 1)

(defn check-monadic-laws
  "TODO generate plain value `v` and the function `f` using spec
  e.g.:
  (check-monadic-laws logtime)
  (map check-monadic-laws [identity-m
                           (writer-m (vector))
                           (writer-m (list))
                           (writer-m (set nil))
                           (writer-m (str))
                           sequence-m maybe-m set-m state-m reader-m cont-m])"
  [monad-m]
  (let [
        v 1
        m v
        n 2
        o 3
        f inc #_alg/+
        hm (cond
             (some (fn [e] (= monad-m e)) #{cont-m})
             (with-monad monad-m
               {:left-identity
                (= ((domonad
                     [x (m-result v)]
                     (f v))
                    inc)
                   ((m-result (f v)) inc))

                :right-identity
                (= ((domonad
                     [x (m-result v)]
                     x)
                    inc)
                   ((m-result v) inc))

                :associativity
                (= ((domonad
                     [y (domonad
                         [x (m-result v)]
                         (f x))]
                     (f y))
                    inc)
                   ((domonad
                     [x (m-result v)
                      y (m-result (f x))]
                     (f y))
                    inc))})

             (some (fn [e] (= monad-m e)) #{state-m reader-m})
             (with-monad monad-m
               {:left-identity
                (= ((domonad
                     [x (m-result v)]
                     (f v))
                    0)
                   ((m-result (f v)) 0))

                :right-identity
                (= ((domonad
                     [x (m-result v)]
                     x)
                    0)
                   ((m-result v) 0))

                :associativity
                (= ((domonad
                     [y (domonad
                         [x (m-result v)]
                         (f x))]
                     (f y))
                    0)
                   ((domonad
                     [x (m-result v)
                      y (m-result (f x))]
                     (f y))
                    0))})

             :else
             (with-monad monad-m
               (let [
                     ;; (>>) :: m a -> m b -> m b
                     ;; m >> k = m >>= (\_ -> k)
                     ;; m >> mzero = mzero
                     m-bind-anything (fn [ma mb] (m-bind ma (fn [_] mb)))
                     ]
                 #_(timbre/debugf "m-zero %s" m-zero)
                 (conj
                  {:left-identity
                   (= (domonad
                       [x (m-result v)]
                       (f v))
                      (m-result (f v)))

                   :right-identity
                   (= (domonad
                       [x (m-result v)]
                       x)
                      (m-result v))

                   :associativity
                   (= (domonad
                       [y (domonad
                           [x (m-result v)]
                           (f x))]
                       (f y))
                      (domonad
                       [x (m-result v)
                        y (m-result (f x))]
                       (f y)))}
                  (when-not (= m-zero :clojure.algo.monads/this-monad-does-not-define-m-zero)
                    {
                     :left-identity-for-zero
                     ;; mzero >>= f  =  mzero
                     (= (domonad
                         [z m-zero]
                         (f z))
                        m-zero
                        (m-bind m-zero (fn [v] (m-result (f v)))))

                     :right-identity-for-zero
                     ;; m >>= (\x -> mzero) == mzero

                     ;; See https://wiki.haskell.org/All_About_Monads
                     ;; The >> function is a convenience operator that is used to bind a
                     ;; monadic computation that does not require input from the previous
                     ;; computation in the sequence. It is defined in terms of >>=:

                     ;; (>>) :: m a -> m b -> m b
                     ;; m >> k = m >>= (\_ -> k)
                     ;; m >> mzero = mzero
                     (= (domonad
                         [z m-zero]
                         z)
                        m-zero
                        (m-bind-anything (m-result v) m-zero))

                     ;; symetry of plus with respect to zero: a + 0 = 0 + a = a
                     :plus-symetry
                     (= (m-plus m-zero (m-result v))
                        (m-plus (m-result v) m-zero)
                        (m-result v))

                     ;; associativity of m-plus
                     :plus-associativity
                     (= (m-plus (m-result m) (m-plus (m-result n) (m-result o)))
                        (m-plus (m-plus (m-result m) (m-result n)) (m-result o)))

                     })))))
        ]
    (if (every? true? (vals hm))
      true hm)))

