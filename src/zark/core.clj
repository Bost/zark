(ns zark.core
  (:use [datomic.api :only [db q] :as d]))

(comment
  ; use following commands on repl
  (in-ns 'zark.core)
  (load "../zark/core")
  ; on real REPL
  (load-file "src/zark/core.clj")
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

;(comment
  ;[:find ?var ?def ?cq ?t
   ;:in $ :where
   ;[?cn :code/name ?var]    ; get all varname, classname, methodname (+sig); ?cn contains codename-entity
   ;[?cq :clj/def ?cn]       ; from the selected codename-entity
   ;[?cq :codeq/code ?def]
   ;[?def :code/text ?t]
   ;]
;)

(def uri "datomic:free://localhost:4334/git")

(def conn (d/connect uri))

(def rules
 '[[(node-files ?n ?f) [?n :node/object ?f] [?f :git/type :blob]]
   [(node-files ?n ?f) [?n :node/object ?t] [?t :git/type :tree]
                       [?t :tree/nodes ?n2] (node-files ?n2 ?f)]
   [(object-nodes ?o ?n) [?n :node/object ?o]]
   [(object-nodes ?o ?n) [?n2 :node/object ?o] [?t :tree/nodes ?n2] (object-nodes ?t ?n)]
   [(commit-files ?c ?f) [?c :commit/tree ?root] (node-files ?root ?f)]
   [(commit-codeqs ?c ?cq) (commit-files ?c ?f) [?cq :codeq/file ?f]]
   [(file-commits ?f ?c) (object-nodes ?f ?n) [?c :commit/tree ?n]]
   [(codeq-commits ?cq ?c) [?cq :codeq/file ?f] (file-commits ?f ?c)]])

(println "Go go go!")

(defn go[]
  (q '[:find ?src (min ?date)
       :in $ % ?name
       :where
       [?n :code/name ?name]
       [?cq :clj/def ?n]
       [?cq :codeq/code ?cs]
       [?cs :code/text ?src]
       [?cq :codeq/file ?f]
       (file-commits ?f ?c)
       (?c :commit/authoredAt ?date)
       ]
     (db conn) rules
     "zark.core/z"))

(go)

