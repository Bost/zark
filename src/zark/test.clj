(defn x [p] (+ 2 2))

(ns zark.core)

(defn y [p]
  ^{ :doc "some doc x" }
  (+ 1 x))

