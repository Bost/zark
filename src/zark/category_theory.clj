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
  A morphish creating a Category Theory object"
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
                (str "Expression is false: (instance? " (.getName cpType) " "
                     (if (instance? String p)
                       (str "\"" p "\"")
                       p)
                       ")")))))))

(def cBool (typeOf Boolean))
(def cObj (typeOf Object))
(def cNum (typeOf Number))
(def cStr (typeOf String))

;; Compile error: Unable to resolve symbol: IPersistentCollection in this context
;; (def gfColl (cTypeOf IPersistentCollection))
(def cColl (typeOf clojure.lang.IPersistentCollection))

(defn cCollOf
  "Creates a contract for clojure.lang.IPersistentCollection object in memory.
  A functor. Takes a morphism (contract) and creates a new morphism (contract).
  pColl must be an instance of clojure.lang.IPersistentCollection"
  [contract]
  (fn [pColl]
    (let [realColl (cColl pColl)] ; make sure coll is a collection
      (doseq [elem realColl]
        (contract elem))         ; make sure every elem fullfils contract
      pColl)))

;;;;;;;;;;;;;;;;;;;;

(defn gfRepeat
  "gf - guarded function; a Morphish"
  [s]
  (let [cs (cStr s)] ;; cs - checked s
    (str cs cs)))

;; contracts with functions they guard form a category

;; (defn double [x]
;; let [cx (cStr x)]
;;     (cStr (str cx"-"cx))))
