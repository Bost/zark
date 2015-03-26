(ns zark.category-theory)

(defn cStr
  "Contract for string objects in memory; a Category Theory object"
  [s]
  (if (instance? java.lang.String s)
    s
    (throw (Exception.
            (str "(instance? java.lang.String " s ") is false)")))))

(defn cAny
  "c - contract; a Category Theory object"
  [x]
  x)

(defn cNum
  "Contract for number objects in memory; a Category Theory object"
  [n]
  (if (instance? java.lang.Number n)
    n
    (throw (Exception.
            (str "(instance? java.lang.Number " n ") is false")))))

(defn cClass
  ;; "Contract for class names; against objects in memory ; a Category Theory object"
  [c]
  (if (instance? java.lang.Class c)
    c
    (throw (Exception.
            (str "(instance? java.lang.Class " c ") is false")))))

(defn typeOf
  "Create contract for objects in memory; a Category Theory object
  pType must be an instance of a java.lang.Class"
  [pType]
  (let [cpType (cClass pType)]
    (fn [p]
      (if (instance? cpType p)
        p
        (throw (Exception.
                (str "Expecting (instance? " cpType " " p ")")))))))

(def cBool (typeOf Boolean))
(def cObj (typeOf Object))
(def cNum (typeOf Number))
(def cStr (typeOf String))

;; Compile error: Unable to resolve symbol: IPersistentCollection in this context
;; (def gfColl (cTypeOf IPersistentCollection))
(def cColl (cTypeOf clojure.lang.IPersistentCollection))

;; (defn cCollOf
;;   "javascript: return arr(a).map(c);"
;;   [contract]
;;   (fn [coll]
;;     (let [realColl (cColl coll)] ; make sure coll is a collection
;;       #(map contract realColl))))

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
