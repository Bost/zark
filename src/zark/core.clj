(ns zark.core)

(comment
; use these commands on repl
(in-ns 'zark.core)
(load "../zark/core")
)

(def fns [* +])

(defn z [p1 result]
  "Function for one parameter p1 and a result"
  (doseq [fi fns]
  (if (= (fi p1) result)
    (println fi p1 "=" result)
    (println fi p1 "not=" result)
    ))
  )

(defn zz [p1 p2 result]
  "Function for two parameters p1 p2 and a result"
  (doseq [fi fns]
  (if (= (fi p1 p2) result)
    (println fi p1 p2 "=" result)
    (println fi p1 p2 "not=" result)
    ))
  )

"Print evaluated expression and return its result"
(defmacro dbg[x]
  `(let [x# ~x]
     (println '~x "=" x#) x#
     )
  )

(def myfns
[
	{:func '"(map #(* 1 %) [4 5 6])" :doc "some desc" }
	{:func '"(map #(* 2 %) [1 2 3])" :doc "other desc"}
	]
)

(defn mval [entry]
 (let [
  func (get entry :func)
  doc (get entry :doc)
  ]
  (println func "=" (eval (read-string func)) "::" doc)
 ))

(defn go []
  (map #(mval %) myfns))

(defmacro m-zz[p1 p2 result]
  `(let [
         p1# ~p1
         p2# ~p2
         result# ~result
         ]
     (zz p1# p2# result#)
     (println '~p1 '~p2"=" result#) result#
     )
  )

(defn generic-z
  ([] nil)
  ([p1] nil)
  ([p1 result]
   (z p1 result)
   )
  ([p1 p2 result]
   (zz p1 p2 result)
   )
  ([p1 p2 p3 & result]
   (println "(defn zzz [p1 p2 p3 & result]: i snot implemented)")
   )
  )

(defn generic [p & args]
  "Just a test function for unspecified arity"
  (let [
        p1 p
        p2 (first args)
        p2next (next args)
        p3 (first p2next)
        p3next (next p2next)
        ]
    (println "p1:" p1 "; p2:" p2 "; p3:" p3 "; p3next:" p3next)
    )
  )

(println "Loaded")
