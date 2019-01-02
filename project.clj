(defproject zark "1.0.0-SNAPSHOT"
  :description "Mighty ZarkOne!"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [
   [org.clojure/clojure "1.10.0"]
   [defun "0.3.0-RC1"] ;; macro to define clojure functions with pattern matching just as erlang or elixir
   [swiss-arrows "1.0.0"] ;; arrow macros
   [com.datomic/datomic-free "0.9.5697" :exclusions [joda-time]]
   ;; see https://dev.clojure.org/jira/browse/CRRBV-18
   [org.clojure/core.rrb-vector "0.0.13"]
   [org.clojure/core.logic "0.8.11"]
   [org.clojure/algo.monads "0.1.6"]

   [frankiesardo/tripod "0.2.0"] ; om-next example app
   [sablono "0.8.4"] ; hiccup style templating for om-next

   ;; webapp - begin
   [re-frame "0.10.6"]
   [secretary "1.2.3"]
   [org.clojure/clojurescript "1.10.439" :exclusions [com.google.guava/guava]]
   [prismatic/om-tools "0.5.0"] ; more convenient dom elements
   [org.omcljs/om "1.0.0-alpha22" :exclusions [commons-codec]] ;; is this needed?

   ;; Ring routing lib; dispatching of GET, PUT, etc.
   ;; describe how a web server communicates with web apps
   ;; and how web apps can be chained together to process one request
   [ring "1.7.1"]
   [compojure "1.6.1"]
   ;; [compojure "1.6.1" :exclusions [commons-codec org.clojure/tools.macro]]
   [garden "1.3.6"] ; render CSS
   [com.andrewmcveigh/cljs-time "0.5.2"] ;; (time/now) in cljs
   ;; webapp - end

   #_[org.clojure/tools.cli "0.3.5"] ; command line arguments

   [org.clojure/java.jdbc "0.7.8"]
   [com.mchange/c3p0 "0.9.5.2"] ; db connection pooling
   [mysql/mysql-connector-java "8.0.13"]
   ;; http://ianrumford.github.io/blog/2012/11/17/first-take-on-contracts-in-clojure/
   [org.clojure/core.contracts "0.0.6"]
   [org.clojure/core.memoize "0.7.1"]

   [org.clojure/core.match "0.3.0-alpha5"] ; pattern matching library

   [clj-time-ext "0.13.0"] ;; (time/now) in clj
   [clj-time "0.15.1"]
   ;; quartzite dependency on slf4j-api should be auto-resolved
   ;; [org.slf4j/slf4j-nop "1.7.13"] ; Simple Logging Facade for Java
   [clojurewerkz/quartzite "2.1.0"] ; scheduling
   ;; [korma "0.4.0"] ;; sql for clojure
   ;; [onelog "0.4.5"]  ;; used also by ring

   ;; [com.draines/postal "1.11.3"]            ; sending emails
   ;; [org.apache.commons/commons-email "1.4"] ; sending emails
   [clj-ssh "0.5.14"]
   [funcool/cuerdas "2.0.6"] ; string manipulation - (str/surround % "'")
   ;; (clojure.core.typed/check-ns) produces 'OutOfMemoryError PermGen space'
   [org.clojure/core.typed "0.6.0" :classifier "slim"] ;; slim vs. fat/ueberjar

   ;; [cauchy-jobs-kestrel "0.1.0"] ;; distributed message queue on the JVM

   ;; Laboratory for Type Theory Experiments
   [latte "0.6.1-SNAPSHOT" #_"0.7.0"]
   ;; Formalization of (typed) Set theory in LaTTe.
   ;; [latte-sets "0.0.7-SNAPSHOT"] ; local installation: lein install

   [funcool/cats "2.3.2"]
   ]
  :plugins
  [
   [lein-figwheel "0.5.18" :exclusions [org.clojure/clojure]]
   [lein-cljsbuild "1.1.7"]
   [lein-garden "0.3.0"]
   ]
  ;; TODO: uncomment for autorun :main zark.core
  ;; The namespace will be auto loaded when a repl is started.

  :source-paths ["src/clj" "src/cljs"]
  :resource-paths ["resources"]
  :clean-targets ^{:protect false} ["resources/public/js/out"
                                    "resources/public/js/main.js"]

  :figwheel
  {:ring-handler zark.server/handler
   ;; Access figwheel server from outside of VM:
   ;; the 'Figwheel: Starting server at http://localhost:3449' is misleading
   ;; :server-ip "10.90.20.167" ; see :websocket-host
   :http-server-root "public" ; css-dirs requires http-server-root specification
   :css-dirs ["resources/public/css"]
   }

  :cljsbuild
  {:builds [{:id "dev"
             :source-paths ["src/cljs" "src/clj"]
             ;; :figwheel {:websocket-host "10.90.20.167"} ; see :server-ip
             :compiler {:output-to "resources/public/js/main.js"
                        :output-dir "resources/public/js/out"
                        :main #_ufo.client zark.core
                        :asset-path "js/out"
                        :source-map true ; debug ClojureScript in the browser
                        :optimizations :none}}]}
  :profiles
  {:uberjar {:aot :all}
   :dev {:dependencies [[figwheel-sidecar "0.5.18"
                         :exclusions [com.google.guava/guava
                                      commons-codec
                                      org.clojure/tools.analyzer
                                      org.clojure/tools.analyzer.jvm]]
                        [org.clojure/test.check "0.9.0"] ; for clojure.spec
                        [com.cemerick/piggieback "0.2.2"]
                        [figwheel-sidecar "0.5.18"]
                        [ns-tracker "0.3.1"]
                        [binaryage/devtools "0.9.10"]
                        [org.clojure/tools.nrepl "0.2.13"]]
         ;; Leads to Error loading cemerick.piggieback ... /queue-eval is not public
         ;; :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
         :source-paths ["src/cljs" "src/clj"]}}

  :garden
  {:builds [{:id "screen"
             :source-paths ["src/clj"]
             :stylesheet ufo.css/screen
             :compiler {:output-to "resources/public/css/style.css"
                        :pretty-print? true}}]
   ;; [org.clojure/tools.nrepl "0.2.13"]
   }
  )
