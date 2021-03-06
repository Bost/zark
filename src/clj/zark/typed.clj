(ns zark.typed
  {:lang :core.typed}
  (:refer-clojure :exclude [fn for])
  (:require [clojure.core.typed :as t]))

(t/defalias Tvn (t/Vec Number))

(t/ann f [t/Any -> Tvn])
(defn f [x]
  (vec (t/for [a :- Number [1 2 3]] :- Number a)))
