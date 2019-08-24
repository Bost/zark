(ns zark.monte-carlo
  "https://rosettacode.org/wiki/Monte_Carlo_methods#Clojure")

(defn inside-circle? [x y]
  (<= (+ (* x x) (* y y))
      1))

(defn sample []
  (inside-circle? (rand) (rand)))

(defn log [v]
  (println "v" v)
  v)

(log (sample))

(repeatedly 1e1 sample)

;; [criterium "0.4.4"]
;; (use 'criterium.core)
;; (bench (repeatedly 1e1 sample))
