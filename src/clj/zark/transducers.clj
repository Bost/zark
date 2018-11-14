(ns zark.transducers
  ;; transform (multiple times) and then reduce
  (:require [clojure.core.async :refer [>! <! <!!] :as a]))

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

(->> (range 10)    ;; seq of values
     (map inc)     ;; mapping
     (filter odd?) ;; mapping
     (reduce + 0)) ;; reducing
;; => 25

(def map-filter
  (comp
   (map inc)
   (filter odd?)))

;; threading macros allow just one level of composition:
(->> (range 10)
     ;; mapping/filtering/removing can invoked step-by-step
     (map inc)
     (filter odd?)
     ;; but it can't be explicitelly composed
     #_
     (comp
      (map inc)
      (filter odd?))
     ;; neither this can't be used
     #_map-filter
     (reduce + 0))
;; => 45

(let [u 1
      v 2]
  (transduce
   (comp
    (map #(+ u %))
    (map #(+ v %)))
   + 0
   (range 5)))
;; => 25

(transduce
 identity
 conj []
 (range 5))
;; => [0 1 2 3 4]

(transduce
 identity
 (fn [& args] (vec (flatten (vector args)))) []
 (range 5))
;; => [0 1 2 3 4]

(transduce
 (comp
  (map inc)
  identity)
 (fn [& args] (vec (flatten (vector args)))) []
 (range 5))
;; => [1 2 3 4 5]

(defn x-fn1 [u v]
  (comp
   (map #(+ u %))
   (map #(+ v %))))

(defn x-fn2 [u v]
  (map (comp
        #(+ u %)
        #(+ v %))))

(let [u 1
      v 2
      vals (range 5)]
  (=
   (transduce (x-fn1 u v) + 0 vals)
   (transduce (x-fn2 u v) + 0 vals)))
;; => true

(def xform (comp (filter odd?) (map inc)))

(defn process [items]
  (let [
        ;; create out-channel with buffer size of 1 and a transducer
        out (a/chan 1 xform)
        ;; create in-channel containing items
        in (a/to-chan items)]
    (a/go-loop [] ;; here the go-loop works sequentially
      ;; take out from the in-channel and store to item
      (if-some [item (<! in)]
        (do
          (>! out item) ;; put item to the out-channel
          (recur))
        (a/close! out)))
    ;; conjoin the content of the out-channel to a vector and return in
    (<!! (a/reduce conj [] out))))

(defn process [items]
  (let [out (a/chan (a/buffer 100))]
    (a/pipeline 4 out xform (a/to-chan items))
    (<!! (a/reduce conj [] out))))

(process (range 10))

(defn log [& [idx]]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result el]
       (let [n-step (if idx (str "idx: Step: " idx ". ") "")]
         (println (format "%sResult: %s, Item: %s" n-step result el)))
       (rf result el)))))

;; sequence coerces (donutit) coll to a (possibly empty) sequence
(sequence (log) [:a :b :c])

(def ^:dynamic *dbg?* false)

(defn comp* [& xforms]
  (apply comp
         (if *dbg?*
           (->>
            (range)
            (map log)
            ;; lazy seq of the fst item in each coll, then the snd etc.
            (interleave xforms))
           xforms)))

(transduce
 (comp*
  (filter odd?)
  (map inc))
 +
 (range 5))
;; 6

(binding [*dbg?* true]
  (transduce
   (comp*
    (filter odd?)
    (map inc))
   +
   (range 5))) ;; (range 5) => (0 1 2 3 4)
