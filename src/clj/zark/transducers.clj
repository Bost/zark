(ns zark.transducers
  ;; transform (multiple times) and then reduce
  (:require
   [clojure.core.async :refer [>! <! <!!] :as async]))

;; (map inc) returns a transducer, i.e. fn expecting one param - an reducing fn
(def inc-and-filter
  "At first (filter odd?) then (map inc) then indentity"
  (comp
   (map inc)
   (filter odd?)
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
  (let [;; create out-channel with buffer size of 1 and a transducer
        out (async/chan 1 xform)
        ;; create in-channel containing items
        in (async/to-chan items)]
    (async/go-loop [] ;; here the go-loop works sequentially
      ;; take out from the in-channel and store to item
      (if-some [item (<! in)]
        (do
          (>! out item) ;; put item to the out-channel
          (recur))
        (async/close! out)))
    ;; conjoin the content of the out-channel to a vector and return in
    (<!! (async/reduce conj [] out))))

(defn process [items]
  (let [out (async/chan (async/buffer 100))]
    (async/pipeline 4 out xform (async/to-chan items))
    (<!! (async/reduce conj [] out))))

(process (range 10))

(defn reducer-log [& [comp-step]]
  (fn [reducer]
    (fn
      ([] (reducer))          ;; e.g. (+)       => 0
      ([init] (reducer init)) ;; e.g. (+ 100)   => 100
      ([init coll-elem]       ;; e.g. (+ 100 1) => 101
       (let [msg (-> [(format "[%s]" (clj-time-ext.core/tstp5))
                      "reducer-init-val" init
                      "composition-step" comp-step
                      "coll-elem-val" coll-elem]
                     utils.core/sjoin)]
         ;; reversed print order of composition-steps (1 0) and timestamps
         #_(let [result (reducer init coll-elem)]
           (if comp-step
             (println msg "result" result))
           result)
         ;; ascending print order of composition-steps (0 1) and timestamps
         (let []
           (if comp-step
             (println msg))
           (reducer init coll-elem)))))))

;; sequence coerces (= press, force) coll to a (possibly empty) sequence
(sequence (reducer-log) [:a :b :c]) ;; => (:a :b :c)

(def ^:dynamic *dbg?* false)

(defn comp* [& xforms]
  (apply comp
         (if *dbg?*
           (->>
            (range)
            (map reducer-log)
            ;; lazy seq of the fst item in each coll, then the snd etc.
            (interleave xforms))
           xforms)))

(transduce
 (comp*
  (filter odd?)
  (map inc))
 + 0
 (range 5)) ;; => 6

(binding [*dbg?* true]
  (transduce
   (comp*
    ;; Composition of the transformer runs right-to-left but builds a
    ;; transformation stack that runs left-to-right
    ;; comp-step 1.
    (map #(+ % 10))
    ;; comp-step 0.
    (map #(* % -1)))
   + 100
   (range 5))) ;; (range 5) => (0 1 2 3 4)

;; transducer fun
(def xform (comp
            (map inc)
            (filter even?)
            (dedupe)
            (mapcat range)
            (partition-all 3)
            (partition-by #(< (apply + %) 7))
            (mapcat flatten)
            (random-sample 1.0)
            (take-nth 1)
            (keep #(when (odd? %) (* % %)))
            (keep-indexed #(when (even? %1) (* %1 %2)))
            (replace {2 "two" 6 "six" 18 "eighteen"})
            (take 11)
            (take-while #(not= 300 %))
            (drop 1)
            (drop-while string?)
            (remove string?)))

(def data (vec (interleave (range 18) (range 20))))

;; lazily transform the data
(sequence xform data)
;; reduce with a transformation (no laziness)
(transduce xform + 0 data)
;; build one collection from a transformation of another, again no laziness
(into [] xform data)
;; create a recipe for a transformation, which can be subsequently sequenced, iterated or reduced
(iteration xform data)
;; transform everything that goes through a channel - same transducer stack!
(let [c (async/chan 1 xform)]
  (async/thread (async/onto-chan c data))
  (async/<!! (async/into [] c)))
