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
  pColl must be an instance of clojure.lang.IPersistentCollection"
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
  "Protocol for Category Theory Objects"
  (cstr [_])
  (valx [_])
  (getOrElse [obj else-val])
  (maybe [m] [m c]
    "Multimethod. m - a monadic value, c - contract. Returns None or Some."))

(deftype None []
  Maybe
  (cstr [_] "None")
  (valx [_] nil)
  (getOrElse [obj else-val] else-val)
  (maybe [m] m))

(deftype Some [x]
  Maybe
  (cstr [_] (str "Some " x))
  (valx [_] x)
  (getOrElse [obj else-val] (valx obj))
  (maybe [m c] (Some. (c (valx m)))))

(defn maybe-alternative
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

(defn twice [functor]
  (fn [c]
    ((functor (functor c)))))

(defn once [functor]
  functor)

(defn once-alt [functor]
  (fn [c]
    (functor c)))

(defn noTimes [functor]
  (fn [c]
    c))

(defn collOfUnit [c]
  (fn [x]
    (let [cx (((noTimes cCollOf) c) x)]        ; input passes the guard c
      (((once cCollOf) c) [cx]))))  ; output passses the guard (cColl c)

(defn maybeUnit-alt [c]
  (fn [x]
    (let [cx ((noTimes cCollOf) (c x))]        ; input passes the guard c
      ((maybe-alternative c) (Some. cx)))))

(defn maybeUnit [c]
  (fn [x]
    (let [cx (c x)]        ; input passes the guard c
      (maybe (Some. x) cx))))

;; TODO maybe

(defn collOfFlatten [c]
  (fn [aax]
    ;; input [[1 2 3] [4 5]] passes the guard c
    (let [caax ((cCollOf (cCollOf c)) aax)]
      ((cCollOf c)
       ;; from decrease the vector dimenssion by 1
       (apply into caax)))))

(defn maybeFlatten-alt [c]
  (fn [mmx]
    (let [cmmx ((maybe-alternative (maybe-alternative c)) mmx)]
      (let [r (cond
                (instance? Some mmx) (valx mmx)
              ;; (instance? None m) mmx ; not needed
              )]
        ((maybe-alternative c) r)))))

(defn maybeFlatten [c]
  (fn [mmx]
    (let [cmmx (maybe (maybe mmx c) c)]
      (let [r (cond
                (instance? Some mmx) (valx mmx)
              ;; (instance? None m) mmx ; not needed
              )]
        (maybe r c) ))))
