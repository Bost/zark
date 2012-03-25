(ns zark.core)

(comment
; use these commands on repl
(in-ns 'zark.core)
(load "../zark/core")
)


(def fns [map apply])

(defn exec [f a b o]
; TODO name of current function: http://www.mail-archive.com/clojure@googlegroups.com/msg13018.html
 (let [
 fi (fn [] (f a b))
 sf (str "(" 'f " " a " " b")")
 ]
  (
   (println (str sf ": " (fi)))
   (if (= (fi) o)
	(println (str "= " sf " " o))
	(println (str "not= " sf " " o))
   ))))

(defn zark [a o]
  (if (= a o)
    (println "=" a o)

    (map #(% a) fns)
    ;(println "not=" a o)
    
    ))

(println "Loaded")
