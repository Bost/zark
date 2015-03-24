(ns zark.category-theory)

(defn contract-str [s]
  (if (= java.lang.String (type s))
    s
    (throw (Exception.
            (str "Expecting (= java.lang.String (type " s "))")))))
