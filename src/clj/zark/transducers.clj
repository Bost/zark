(ns zark.transducers
  ;; transform (multiple times) and then reduce
  (:require [clojure.core.async :as a]))

;; (map inc) returns a transducer, i.e. a fn expecting one param - an reducing fn
(def inc-and-filter
  (comp
   (map inc)     ;; 1st
   (filter odd?) ;; 2nds
   identity))

(def special+ (inc-and-filter +))
(special+ 1 1)
;; 1
(special+ 1 2)

(reduce + 0 (range 10)) ;; 45
(reduce special+ 0 (range 10)) ;; 25
(reduce + 0 (filter odd? (map inc (range 10)))) ;; 25
(reduce + 0 (->> (range 10)
                 (map inc)
                 (filter odd?))) ;; 25

(->> (range 10)
     (map inc)
     (filter odd?)
     (reduce + 0)) ;; 25


(->> (range 10)
     ;; following can't be composed
     #_
     (comp
      (map inc)
      (filter odd?))
     (reduce + 0))
