(defproject zark "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.datomic/datomic-free "0.9.5153" :exclusions [joda-time]]
                 ]
  :plugins [
            [cider/cider-nrepl "0.9.0-SNAPSHOT"]
            [refactor-nrepl "0.3.0-SNAPSHOT"]
            ]
  ;; TODO: uncomment this for autorun :main zark.core
  ;; This namespace will get loaded automatically when you launch a repl.
  )
