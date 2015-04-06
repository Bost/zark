(ns zark.category-theory)

(defn c-str
  "Contract for java.lang.String objects in memory.
  A morphish creating a Category Theory object"
  [s]
  (if (instance? java.lang.String s)
    s
    (throw (Exception.
            (str "(instance? java.lang.String " s ") is false)")))))

(defn c-any
  "Contract for any object in memory.
  A morphish creating a Category Theory object
  TODO create tests for c-any"
  [x]
  x)

(defn c-num
  "Contract for java.lang.Number objects in memory.
  A morphish creating a Category Theory object"
  [n]
  (if (instance? java.lang.Number n)
    n
    (throw (Exception.
            (str "(instance? java.lang.Number " n ") is false")))))

(defn c-class
  "Contract for java.lang.Class objects in memory.
  A morphish creating a Category Theory object"
  [c]
  (if (instance? java.lang.Class c)
    c
    (throw (Exception.
            (str "(instance? java.lang.Class " c ") is false")))))

(defn encode [p]
  (cond
    (nil? p) "nil"
    (instance? String p) (str "\"" p "\"")
    :else p))

(defn type-of
  "Create contracts for java.lang.Class objects in memory;
  A parametrized morphish creating Category Theory objects.
  p-type must be an instance of java.lang.Class"
  [p-type]
  (let [cp-type (c-class p-type)]
    (fn [p]
      (if (instance? cp-type p)
        p
        (throw (Exception.
                (str "Expression is false: (instance? "
                     (.getName cp-type)
                     " " (encode p) ")")))))))

(def c-bool (type-of Boolean))
(def c-objm (type-of Object))
(def c-num (type-of Number))
(def c-str (type-of String))

;; Compile error: Unable to resolve symbol: IPersistentCollection in this context
;; (def gfColl (cTypeOf IPersistentCollection))
(def c-coll (type-of clojure.lang.IPersistentCollection))

(defn c-coll-of
  "Creates a contract for clojure.lang.IPersistentCollection object in memory.
  A functor. Takes a morphis (guarded fn) / CT object (contract) and creates
  a new morphis (guarded fn) / CT object (contract).
  p-coll must be an instance of clojure.lang.IPersistentCollection"
  [contract]
  (fn [p-coll]
    (let [real-coll (c-coll p-coll)] ; make sure coll is a collection
      (into (empty real-coll)
            ;; make sure every elem fullfills contract
            (map contract real-coll)))))

;;;;;;;;;;;;;;;;;;;;

(defn gf-repeat
  "gf - guarded function; a Morphish"
  [s]
  (let [cs (c-str s)] ;; cs - checked s
    (str cs cs)))

;; contracts with functions they guard form a category

(defn notimes [functor]
  (fn [c]
    c))

(defn notimes-alt [functor]
  (fn [c]
    c))

(defn once [functor]
  functor)

(defn once-alt [functor]
  (fn [c]
    (functor c)))

(defprotocol Maybe
  "Protocol for Category Theory Objects"
  (cstr [_])
  (valx [_])
  (get-or-else [obj else-val])
  (maybe-flatten [mmx] [mmx c]
    "Multimethod. m - a monadic value (maybe-maybe-x), "
    "c - contract. Returns None or Some.")
  (maybe [m] [m c]
    "Multimethod. m - a monadic value, c - contract. Returns None or Some."))

(deftype None []
  Maybe
  (cstr [_] "None")
  (valx [_] nil)
  (get-or-else [obj else-val] else-val)
  (maybe-flatten [mmx] mmx) ; i.e. identity fn
  (maybe [m] m))

(defn twice [functor mmx c]
  (functor (functor mmx c) c))

(defn twice-fn [functor]
  (fn [mmx c]
    (functor (functor mmx c) c)))

(deftype Some [x]
  Maybe
  (cstr [_] (str "Some " x))
  (valx [_] x)
  (get-or-else [obj else-val] (valx obj))
  
  (maybe [m c] (Some. (c (valx m))))
  
  (maybe-flatten
    [mmx] (maybe-flatten mmx c-any))
  
  (maybe-flatten
    [mmx c] ; maybe-maybe-x
    (let [c-mmx ((twice-fn maybe) mmx c)
          ;; TODO test if (apply into c-mmx) works for any kind of collections
          flat-val (apply into (valx c-mmx))]
      (Some. flat-val))))

(defn maybe-alt
  "Functor. Can be used as an alternative to throwing an exception.
  No need to wrap everything in an try-catch block."
  [c]
  (fn [m]
    (cond
      (instance? None m) m
      (instance? Some m) (Some. (c (valx m)))
      :else (throw (Exception.
                    (str "Expression is false: "
                         "(or (instance? (None.) " (encode m) ")"
                         " (instance? (Some. \"\") " (encode m) "))"))))))

(defn maybe-unit [c]
  (fn [x]
    ;; check if input passed the contract c
    (let [c-x ((notimes-alt maybe) x c)]
      (maybe (Some. x) c-x))))

(defn coll-unit [x]
  [x])

(defn coll-of-unit [c]
  (fn [x]
    (let [c-x (((notimes-alt c-coll-of) c) x)]        ; input passes the guard c
      (((once c-coll-of) c) [c-x]))))  ; output passses the guard (c-coll c)

(defn maybe-unit [c]
  (Some. c))

(defn maybe-unit-alt [c]
  (fn [x]
    ;; check if input passed the contract c
    (let [c-x (((notimes-alt maybe-alt) c) x)]
      (((once maybe-alt) c) (Some. c-x)))))

(defn coll-of-flatten [c]
  (fn [ccx] ; collection-of-collections-of-x
    ;; input [[1 2 3] [4 5]] passes the guard c
    (let [c-ccx ((c-coll-of (c-coll-of c)) ccx)]
      ((c-coll-of c)
       ;; from decrease the vector dimenssion by 1
       (apply into c-ccx)))))

(defn coll-flatten
  ([]
   (coll-of-flatten c-any))
  ([c]
   (coll-of-flatten c)))
