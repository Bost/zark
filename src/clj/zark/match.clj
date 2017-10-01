(ns ^{:doc "See William Byrd: The Most Beautiful Program Ever Written
https://youtu.be/OyfBQmvr2Hc"}
    zark.match
  (:require
   [clojure.core.match :refer [match]]))

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
(eval-expr '(((fn [x] x) (fn [y] y)) 5) env)
(eval-expr '(inc 1) env)
(eval-expr '(+ 1 2 3) env)
