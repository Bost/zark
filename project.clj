(defproject zark "1.0.0-SNAPSHOT"
  :description "Mighty ZarkOne!"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [[org.clojure/clojure "1.8.0"]
   [com.datomic/datomic-free "0.9.5372" :exclusions [joda-time]]
   [org.clojure/core.logic "0.8.10"]
   [org.clojure/algo.monads "0.1.5"]

   ;; Kestrel - distributed message queue on the JVM
   ;; [cauchy-jobs-kestrel "0.1.0"]
   ]
  :plugins
  [[lein-cljfmt "0.2.0"]]
  ;; TODO: uncomment for autorun :main zark.core
  ;; The namespace will be auto loaded when a repl is started.
  )
