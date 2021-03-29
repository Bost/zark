(ns zark.transducers
  {:doc
   "Transform any sequence (seq, async channel, maybe observable), possibly
   multiple times and then reduce it."}
  (:require
   [clj-time-ext.core]
   [utils.core]
   [clojure.core.async :refer [>! <! <!!] :as async]))

;; TODO literate programming in clojure
;; https://github.com/gdeer81/marginalia

;; https://groups.google.com/forum/#!topic/clojure/FXMCgqfLiuE

(def inc-and-filter-xform
  "`(map inc)` and `(filter odd?)` are transducing functions, a.k.a xforms, xfs.

  Transducer signature:
  `(typeX, input -> typeX) -> (typeY, input -> typeY)` I.e. transducer is a
  function of one argument. The type of this argument is an arrow-type, i.e. it
  is function itself and it has two arguments.

  Ordering of transducer fns in `comp` is the same order as sequence
  transformations in `->>`. I.e. at first `(map inc)` then `(filter odd?)`.

  However ordering of a plain `comp` is reversed:
  ((comp str inc) 1)    ;; => 2
  ;; ((comp inc str) 1) ;; exception: String cannot be cast to Number

  In the end the composition returns a transducer (xform, xf) itself, i.e. a
  function of one argument."
  (comp (map inc)
        (filter odd?)))

(def special+
  "Applies the `inc-and-filter-xform` on the reducing function `+` and creates a
  function of the same type as the `+` function."
  (inc-and-filter-xform +))

(special+ 1 1) ;; => 1
(special+ 1 2) ;; => 4

(reduce +        0 (range 10))                         ;; => 45
(reduce special+ 0 (range 10))                         ;; => 25
(reduce +        0 (filter odd? (map inc (range 10)))) ;; => 25
(reduce +        0 (->> (range 10)
                        (map inc)
                        (filter odd?)))                ;; => 25

(->> (range 10)    ;; seq of values
     (map inc)     ;; mapping
     (filter odd?) ;; mapping
     (reduce + 0)) ;; reducing
;; => 25

;; threading macros allow just one level of composition:
(->> (range 10)
     ;; `map`, `filter`, `remove` can be invoked step-by-step
     (map inc)
     (filter odd?)
     ;; but they can't be explicitly composed:
     ;; (comp (map inc) (filter odd?))  ;; gives error
     (reduce + 0))
;; => 45

(let [u 1
      v 2]
  (transduce (comp
              (map #(+ u %))
              (map #(+ v %)))
             + 0
             (range 5)))
;; => 25

(def xform (comp (map inc) (map str)))
(transduce xform conj [] (range 5)) ;; => ["1" "2" "3" "4" "5"]
;; `into` also can accepts a transducer:
(into [] xform (range 5))           ;; => ["1" "2" "3" "4" "5"]

(transduce identity
 (fn [& args]
   (vec (flatten (vector args)))
   #_((comp vec flatten vector) args)
   #_(->> args ((comp vec flatten vector)))
   #_(->> args (vector) (flatten) (vec)))
 []
 (range 5))
;; => [0 1 2 3 4]

(transduce
 (comp (map inc) identity)                   ;; the xform
 (fn [& args] (vec (flatten (vector args)))) ;; reducing fn (a.k.a reducer)
 []                                          ;; init val of the reducer
 (range 5))                                  ;; the coll to transform & reduce
;; => [1 2 3 4 5]

(defn x-fn1 [u v] (comp (map #(+ u %))
                        (map #(+ v %))))

(defn x-fn2 [u v] (map (comp #(+ u %)
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

(defn xf-sort
  "A sorting transducer. Mostly a syntactic improvement to allow composition of
  sorting with the standard transducers, but also provides a slight performance
  increase over transducing, sorting, and then continuing to transduce."
  ([]
   (xf-sort compare))
  ([cmp]
   (fn [rf]
     (let [temp-list (java.util.ArrayList.)]
       (fn
         ([]
          (rf))
         ([xs]
          (reduce rf xs (sort cmp (vec (.toArray temp-list)))))
         ([xs x]
          (.add temp-list x)
          xs))))))

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
;; => [0 0 1 1 2 2 3 3 4 4 5 5 6 6 7 7 8 8 9 9 10 10 11 11 12 12 13 13 14 14 15
;; 15 16 16 17 17]

;; lazily transform the data
(sequence xform data)
;; reduce with a transformation (no laziness)
(transduce xform + 0 data)
;; build one collection from a transformation of another, again no laziness
(into [] xform data)
;; create a recipe for a transformation, which can be subsequently sequenced, iterated or reduced
(iterate xform data)
;; transform everything that goes through a channel - same transducer stack!
(let [c (async/chan 1 xform)]
  (async/thread (async/onto-chan c data))
  (async/<!! (async/into [] c)))

;; Different possibilities how write dataflow queries https://stackoverflow.com/a/26322910
;; 1. nested calls:
(reduce + (filter odd? (map #(+ 2 %) (range 0 10))))

;; 2. functional composition:
(def xform
  (comp
   (partial filter odd?)
   (partial map #(+ 2 %))))
(reduce + (xform (range 0 10)))

;; 3. threading macro:
(defn xform [xs]
  (->> xs
       (map #(+ 2 %))
       (filter odd?)))
(reduce + (xform (range 0 10)))

;; 4. transducers:
(def xform
  (comp
   (map #(+ 2 %))
   (filter odd?)))
(transduce xform + (range 0 10))

;; See https://clojure.org/guides/equality
(def m=
  "(BUGGY) Equality transducer. Check that all collection elements are equal.
  Contains `=`. Replacement with `m=` leads to StackOverflowError."
  (fn
    ([]
     (let [r ::init]
       #_(println "[Init      ]" "r:" r)
       r))
    ([result]
     (let [r result]
       #_(println "[Completion]" "result:" result "r:" r)
       (not= result ::false)))
    ([result input]
     (let [r (if (or (= result input)
                     (= result ::init))
               input
               ::false)]
       #_(println "[Step      ]" "result:" result "input:" input "r:" r)
       r))))

(transduce identity m= [])    ;; => true
(transduce identity m= [1])   ;; => true
(transduce identity m= [1 1]) ;; => true
(transduce identity m= [1 2]) ;; => false
(= true (m= []))    ;; => true
(= true (m= [1]))   ;; => true
(= true (m= [1 1])) ;; => true
;; (= false (m= [1 2])) ;; => true !!! BUG

(defn m=
  [coll]
  (let [[head & tail] coll]
    (every? (fn [e] (= e head)) tail)))

(defn m=
  [coll]
  (or (empty? coll)
      (apply = coll)))

(defn m=
  [coll]
  (or (empty? coll)
      (= (count (set coll)) 1)))

(m= [])    ;; => true
(m= [1])   ;; => true
(m= [1 1]) ;; => true
(m= [1 2]) ;; => false
;; (transduce identity m= [])    ;; throws exception
;; (transduce identity m= [1])   ;; throws exception
;; (transduce identity m= [1 1]) ;; throws exception
;; (transduce identity m= [1 2]) ;; throws exception

(let [f +
      coll []
      ;; coll nil
      xf map
      ]
  (apply xf [f coll]))

(defn f [])
(= (identity f) f)

;; Ugh 1.
(= (identity (defn f [])) (defn f [])) ;; => true
(= (identity (fn f [])) (fn f []))       ;; => false

;; Ugh 2.
((comp identity +)) ;; => 0
;; ((comp + identity)) ;; throws exception

;; Ugh 3.
(+) ;; => 0
;; (-) ;; throws exception

;; (=) ;; throws exception
;; (*) ;; throws exception
;; (/) ;; throws exception

;; Ugh 4.
;; (boolean) ;; throws exception
;; (not) ;; throws exception
(or)  ;; => nil
(and) ;; => true

;; (bit-or)  ;; exception
;; (bit-and) ;; exception

;; (bit-and 1) ;; exception
(and 1)     ;; => 1

;; TODO
every?
not-any?

;; several transducers can be given, without using 'comp'
(eduction (filter even?) (map inc)
          (range 5))
;; TODO see https://ask.clojure.org/index.php/8654/transducers-and-maps?show=8654#q8654
;; => (1 3 5)

(sequence
 (eduction (filter even?) (map inc)
           (range 5)))
;; => (1 3 5)

;; transducer pipeline debugging:
;; Renzo Borgatti - Clojure Transducers In The Wild
;; https://youtu.be/Tn05cXrhBvg?t=1311

;; https://github.com/cgrand/xforms/ issue 39:
;; When I think about it again and take a look at the [transducer type signature](https://clojure.org/reference/transducers#_creating_transducers) then my original question may not be completely wrong. Hmm. Except that, the reduction process should be stopped using `reduced`. So here again:
;; ```clojure
;; (defn first
;;   ([] nil)
;;   ([x] x)
;;   ([fst lst] (reduced fst)))
;; ```
;; Another thing, the [`sum`](https://github.com/cgrand/xforms/blob/62375212a8604daad631c9024e9dbe1db4ec276b/src/net/cgrand/xforms/rfs.cljc#L105) function doesn't return *first* logical value:
;; ```clojure

;; ```

(defn dbg-last
  "Reducing function that returns the last value."
  ([]
   (println ";; => nil")
   nil)
  ([x]
   (println ";; =>" x)
   x)
  ([fst lst]
   (println ";; fst" fst "lst" lst "=>" lst "; reduction continues...")
   lst))

(defn dbg-first
  ([]
   (println ";; => nil")
   nil)
  ([x]
   (println ";; =>" x)
   x)
  ([fst lst]
   (println ";; fst" fst "lst" lst "=>" (reduced fst) "; stopping reduction")
   #_fst
   (reduced fst)))

(defn dbg-some
  "Reducing function that returns the first logical true value."
  ([]
   (println ";; => nil")
   nil)
  ([x]
   (println ";; =>" x)
   x)
  ([fst lst]
   (println ";; fst" fst "lst" lst "=>" (when lst (reduced lst)) "; stopping reduction")
   (when lst (reduced lst))))

;; See https://github.com/cgrand/xforms/ issues 39
(require
 '[net.cgrand.xforms :as x]
 '[net.cgrand.xforms.rfs :as rfs])

(into {} (x/by-key identity rfs/last) [1 2 3 4]) ;; => {1 1, 2 2, 3 3, 4 4}
(into {} (x/by-key identity x/last) [1 2 3 4])    ;; => {1 1, 2 2, 3 3, 4 4}
(into {} (x/by-key identity rfs/some) [1 2 3 4]) ;; => {1 1, 2 2, 3 3, 4 4}
;; throws 'Wrong number of args (1) passed to: net.cgrand.xforms/some'
;; (into {} (x/by-key identity x/some) [1 2 3 4])

;; Also:

(reduce rfs/last [1 2 3 4]) ;; => 4
(reduce rfs/some [1 2 3 4]) ;; => returns 2; I'd expect 1
(x/some identity [1 2 3 4]) ;; => 1
;; throws 'Wrong number of args (2) passed to: net.cgrand.xforms/reduce/fn--35656'
;; (x/last identity [1 2 3 4])

;; And here I'd expect same types:

(type (rfs/last identity [1 2 3 4])) ;; => clojure.lang.PersistentVector
(type (rfs/some identity [1 2 3 4])) ;; => clojure.lang.Reduced
