(ns ^{:doc "See William Byrd: The Most Beautiful Program Ever Written
https://youtu.be/OyfBQmvr2Hc"}
    zark.match
  (:require
   [clojure.core.match :refer [match]]
   [defun.core :refer [defun]]))

(defn eval-expr [expr env]
  #_(println "expr:" expr "type:" (type expr))
  (match
   [expr]
   [(n :guard number?)]
   n

   ;; [(['zero? e] :seq)] (zero? (eval-expr e env))
   ;; [(['inc e] :seq)] (inc (eval-expr e env))
   ;; [(['if t c a] :seq)] (if (eval-expr t env) (eval-expr c env) (eval-expr a env))
   ;; [(['* e1 e2] :seq)] (* (eval-expr e1 env) (eval-expr e2 env))

   ;; [([(fnx :guard #(fn? (eval %))) e1] :seq)]
   ;; ((eval fnx) (eval-expr e1 env))

   ;; [([(fnx :guard #(fn? (eval %))) e1 e2] :seq)]
   ;; ((eval fnx) (eval-expr e1 env) (eval-expr e2 env))

   ;; [([(fnx :guard #(fn? (eval %))) e1 e2 e3] :seq)]
   ;; ((eval fnx) (eval-expr e1 env) (eval-expr e2 env) (eval-expr e3 env))

   [(x :guard symbol?)] (env x) ;; env is a function of 1 argument

   [([(fnx :guard #(fn? (eval %))) & r] :seq)]
   (apply (eval fnx) (map #(eval-expr % env) r))

   [(['fn [x] body] :seq)] (fn [arg] (eval-expr body
                                              (fn [y] (if (= x y) arg (env y)))))
   [([op-rator op-rand] :seq)] ((eval-expr op-rator env)
                                (eval-expr op-rand env))))

(def env (fn [expr] (println "Unbound expr:" expr)))
;; (eval-expr '(((fn [x] x) (fn [y] y)) 5) env)
;; (eval-expr '(inc 1) env)
;; (eval-expr '(+ 1 2 3) env)

;; TODO recursion as a start-stop-work pattern: search for 'recur' in the code.
(defun my-zipmap
  ([keys vals] (recur {} (seq keys) (seq vals))) ;; start
  ([map ks :guard empty? vs              ] map)  ;; stop
  ([map ks               vs :guard empty?] map)  ;; stop
  ([map ks               vs              ]       ;; work
   (recur (assoc map (first ks) (first vs))
          (next ks) (next vs))))

;; (letfn [(twice [x]
;;           (* x 2))
;;         (six-times [y]
;;           (* (twice y) 3))]
;;   (println "Twice 15 =" (twice 15))
;;   (println "Six times 15 =" (six-times 15)))

;; A contrived example of mutual recursion
(defn even2? [n]
  (letfn [(neven? [n] (if (zero? n) true (nodd? (dec n))))
          (nodd? [n] (if (zero? n) false (neven? (dec n))))]
    (neven? n)))

;; TODO does M(x) halt withing N steps: is it better to compute vals of have tables with precomputed vals.

