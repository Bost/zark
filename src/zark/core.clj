(ns zark.core)

(comment
  ; use following commands on repl
  (in-ns 'zark.core)
  (load "../zark/core")
  )

(def fns '[* +])

(defn z [p1 res]
  ^{ :doc "Apply all functions from fns to p1, each time compare result with res
          and print comparision result" }
  (doseq [fi fns]
    (let [
          s (str "(" fi " " p1 ")")
          ]
      (if (= ((eval fi) p1) res)
        (println s "==" res)
        (println s "!=" res)))))

(z 2 1)
(z 2 2)
(z 2 3)

(defn zz [p1 p2 res]
  ^{:doc "Apply all functions from fns to p1 and p2, each time compare result
         with res and print comparision result" }
  (doseq [fi fns]
    (let [
          s (str "(" fi " " p1 " " p2 ")")
          ]
      (if (= ((eval fi) p1 p2) res)
        (println s "==" res)
        (println s "!=" res)))))

(zz 2 2 2)
(zz 2 2 4)
(zz 2 2 5)

"Print evaluated expression and return its res"
(defmacro dbg[x]
  `(let [x# ~x]
     (println '~x "=" x#) x#
     ))

(def myfns [
            {:func '"(map #(* 1 %) [4 5 6])" :doc "some desc" }
            {:func '"(map #(* 2 %) [1 2 3])" :doc "other desc"}
            ])

(defn mval [entry]
  (let [ func (get entry :func)
        doc (get entry :doc) ]
    (println func "=" (eval (read-string func)) "::" doc)))

(defn go []
  (map #(mval %) myfns))

(defmacro m-zz[p1 p2 res]
  `(let [
         p1# ~p1
         p2# ~p2
         res# ~res
         ]
     (zz p1# p2# res#)
     (println '~p1 '~p2"=" res#) res#))

(defn generic-z
  ([] nil)
  ([p1] nil)
  ([p1 res]
   (z p1 res))
  ([p1 p2 res]
   (zz p1 p2 res))
  ([p1 p2 p3 & res]
   (println "(defn zzz [p1 p2 p3 & res]: is not implemented)")))

(defn generic [p & args]
  "Just a test function for unspecified arity"
  (let [
        p1 p
        p2 (first args)
        p2next (next args)
        p3 (first p2next)
        p3next (next p2next)
        ]
    (println "p1:" p1 "; p2:" p2 "; p3:" p3 "; p3next:" p3next)))

(defn inc-if-match [item cnt sample]
  ^{:doc "Return cnt+1 if the item is 'defn; otherwise return cnt" }
  (if (= item sample)
    (inc cnt)
    cnt))

(defn cnt-samples [coll sample]
  ^{:doc "Return a count of samples in the coll" }
  ;loop binds initial values once then binds values from each recursion call
  (loop [coll coll
         cnt 0
         sample sample]
    (if (empty? coll)
      cnt
      (recur (rest coll)
             (inc-if-match (first coll) cnt sample) sample))))

(cnt-samples '(1 defn 3 2 defn 2 defn) 'defn)

(println "Loaded")

(def f (slurp "/home/bost/dev/zark/src/zark/test.clj"))
(def lf (read-string f))

(println "lf: " lf)
(println "samples: " (cnt-samples lf 'defn))

