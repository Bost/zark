(ns zark.category-theory)

(defn cStr
  "Contract for java.lang.String objects in memory.
  A morphish creating a Category Theory object"
  [s]
  (if (instance? java.lang.String s)
    s
    (throw (Exception.
            (str "(instance? java.lang.String " s ") is false)")))))

(defn cAny
  "Contract for any object in memory.
  A morphish creating a Category Theory object
  TODO create tests for cAny"
  [x]
  x)

(defn cNum
  "Contract for java.lang.Number objects in memory.
  A morphish creating a Category Theory object"
  [n]
  (if (instance? java.lang.Number n)
    n
    (throw (Exception.
            (str "(instance? java.lang.Number " n ") is false")))))

(defn cClass
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

(defn typeOf
  "Create contracts for java.lang.Class objects in memory;
  A parametrized morphish creating Category Theory objects.
  pType must be an instance of java.lang.Class"
  [pType]
  (let [cpType (cClass pType)]
    (fn [p]
      (if (instance? cpType p)
        p
        (throw (Exception.
                (str "Expression is false: (instance? "
                     (.getName cpType)
                     " " (encode p) ")")))))))

(def cBool (typeOf Boolean))
(def cObj (typeOf Object))
(def cNum (typeOf Number))
(def cStr (typeOf String))

;; Compile error: Unable to resolve symbol: IPersistentCollection in this context
;; (def gfColl (cTypeOf IPersistentCollection))
(def cColl (typeOf clojure.lang.IPersistentCollection))

(defn cCollOf
  "Creates a contract for clojure.lang.IPersistentCollection object in memory.
  A functor. Takes a morphis (guarded fn) / CT object (contract) and creates
  a new morphis (guarded fn) / CT object (contract).
  pColl must be an instance of clojure.lang.IPersistentCollection
  TODO create tests for cCollOf working with morphisms (guarded fns)"
  [contract]
  (fn [pColl]
    (let [realColl (cColl pColl)] ; make sure coll is a collection
      (into (empty realColl)
            ;; make sure every elem fullfills contract
            (map contract realColl)))))

;;;;;;;;;;;;;;;;;;;;

(defn gfRepeat
  "gf - guarded function; a Morphish"
  [s]
  (let [cs (cStr s)] ;; cs - checked s
    (str cs cs)))

;; contracts with functions they guard form a category

(defprotocol Maybe
  (cstr [_])
  (maybe [c]))

(deftype None []
  Maybe
  (cstr [_] "None")
  (maybe [c] (fn [m] m)))

(deftype Some [x]
  Maybe
  (cstr [_] (str "Some " x))
  (maybe [c] (fn [m]
               (Some. (c m))
               )))


(defprotocol P
  (foo [x])
  (bar-me [x] [x y]))

(deftype Foo [a b c]
  P
  (foo [x] a)
  (bar-me [x] b)
  (bar-me [x y] (+ c y)))

(bar-me (Foo. 1 2 3) 42)
;; => 45

(foo
 (let [x 42]
   (reify P
     (foo [this] 17)
     (bar-me [this] x)
     (bar-me [this y] x))))
