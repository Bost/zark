(defproject zark "1.0.0-SNAPSHOT"
  :description "Mighty ZarkOne!"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.datomic/datomic-free "0.9.5206" :exclusions [joda-time]]
                 [org.clojure/core.logic "0.8.10"]                 
                 ]
  :plugins [
            [lein-cljfmt "0.2.0"]
            ]
  ;; TODO: uncomment this for autorun :main zark.core
  ;; This namespace will get loaded automatically when you launch a repl.
  )
