(defproject zark :lein-v
  :description "Mighty ZarkOne!"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [
   [org.clojure/clojure "1.10.1"]
   [org.clojure/spec.alpha "0.2.176"]
   [defun "0.3.1"] ;; macro to define clojure functions with pattern matching just as erlang or elixir
   [swiss-arrows "1.0.0"] ;; arrow macros
   [com.datomic/datomic-free "0.9.5697" :exclusions [joda-time]]
   ;; see https://dev.clojure.org/jira/browse/CRRBV-18
   [org.clojure/core.rrb-vector "0.1.1"]
   [org.clojure/core.logic "0.8.11"]
   [org.clojure/algo.monads "0.1.6"]

   [frankiesardo/tripod "0.2.0"] ; om-next example app
   [sablono "0.8.6"] ; hiccup style templating for om-next

   ;; webapp - begin
   [re-frame "0.10.9"]
   [secretary "1.2.3"]
   [org.clojure/clojurescript "1.10.520" :exclusions [com.google.guava/guava]]
   [prismatic/om-tools "0.5.0"] ; more convenient dom elements
   [org.omcljs/om "1.0.0-alpha22" :exclusions [commons-codec]] ;; is this needed?

   ;; Ring routing lib; dispatching of GET, PUT, etc.
   ;; describe how a web server communicates with web apps
   ;; and how web apps can be chained together to process one request
   [ring "1.7.1"]
   [compojure "1.6.1"]
   ;; [compojure "1.6.1" :exclusions [commons-codec org.clojure/tools.macro]]
   ;; [garden "1.3.9"] ; render CSS; see the :garden config below pf
   [com.andrewmcveigh/cljs-time "0.5.2"] ;; (time/now) in cljs
   ;; webapp - end

   #_[org.clojure/tools.cli "0.3.5"] ; command line arguments

   [org.clojure/java.jdbc "0.7.10"]
   [com.mchange/c3p0 "0.9.5.4"] ; db connection pooling
   ;; TODO https://github.com/LauJensen/clojureql
   [mysql/mysql-connector-java "8.0.18"]
   ;; http://ianrumford.github.io/blog/2012/11/17/first-take-on-contracts-in-clojure/
   [org.clojure/core.contracts "0.0.6"]
   [org.clojure/core.memoize "0.7.2"]

   [org.clojure/core.match "0.3.0"] ; pattern matching library

   [utils "0.0.0-21-0xd148"] ;; pulls in also clj-time-ext

   ;; quartzite dependency on slf4j-api should be auto-resolved
   ;; [org.slf4j/slf4j-nop "1.7.13"] ; Simple Logging Facade for Java
   [clojurewerkz/quartzite "2.1.0"] ; scheduling
   ;; [korma "0.4.0"] ;; sql for clojure
   ;; [onelog "0.4.5"]  ;; used also by ring

   ;; [com.draines/postal "1.11.3"]            ; sending emails
   ;; [org.apache.commons/commons-email "1.4"] ; sending emails
   [clj-ssh "0.5.14"]
   [funcool/cuerdas "2.2.0"] ; string manipulation - (str/surround % "'")
   ;; (clojure.core.typed/check-ns) produces 'OutOfMemoryError PermGen space'
   [org.clojure/core.typed "0.6.0" :classifier "slim"] ;; slim vs. fat/ueberjar

   ;; [cauchy-jobs-kestrel "0.1.0"] ;; distributed message queue on the JVM

   ;; Laboratory for Type Theory Experiments
   [latte "1.0b1-SNAPSHOT"]
   ;; The core "standard" library for the LaTTe proof assistant.
   [latte-prelude "0.2.0-SNAPSHOT"]
   ;; Formalization of (typed) Set theory in LaTTe.
   [latte-sets "0.7.0-SNAPSHOT"]
   ;;Formalization of integers in LaTTe.
   [latte-integers "0.9.0-SNAPSHOT"]

   [funcool/cats "2.3.2"]

   ;; logging doesn't work out-of-the-box
   ;; [org.clojure/tools.logging "0.4.1"]
   ;; debug single- and multi-threaded apps
   ;; [spyscope "0.1.6"]

   [org.python/jython-standalone "2.7.1"]
   ;; Clojure Jython interop
   [clojure-python "1.0.0"]
   [midje "1.9.9"]


   [halfling "1.2.1"]
   ]
  :plugins
  [;; a plugin to tell you your code is bad, and that you should feel bad
   [lein-bikeshed "0.5.2"]
   ;; Drive leiningen project version from git instead of the other way around
   [com.roomkey/lein-v "7.1.0"]
   [lein-figwheel "0.5.19" :exclusions [org.clojure/clojure]]
   [lein-cljsbuild "1.1.7"]
   ;; autocompile theGarden stylesheets - see the [garden "..."] dependency
   [lein-garden "0.3.0"]
   ;; autorecompile changed java files
   [lein-virgil "0.1.9"]]
  ;; Using :java-source-paths causes:
  ;;         'recompiling all files in ...'
  ;; and subsequent error:
  ;;         'REPL server launch timed out'
  ;; :java-source-paths ["javasrc"]
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
   :css-dirs ["resources/public/css"]}

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
   :dev {:dependencies [[figwheel-sidecar "0.5.19"
                         :exclusions [com.google.guava/guava
                                      commons-codec
                                      org.clojure/tools.analyzer
                                      org.clojure/tools.analyzer.jvm]]
                        [org.clojure/test.check "0.10.0"] ; for clojure.spec
                        [cider/piggieback "0.4.2"]
                        ;; [com.cemerick/piggieback "0.2.2"]
                        [ns-tracker "0.4.0"]
                        [binaryage/devtools "0.9.10"]
                        ]
         ;; Leads to Error loading cemerick.piggieback ...
         ;; :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
         :source-paths ["src/cljs" "src/clj"]}}
  ;; :garden
  ;; {:builds [{:id "screen"
  ;;            :source-paths ["src/clj"]
  ;;            :stylesheet ufo.css/screen
  ;;            :compiler {:output-to "resources/public/css/style.css"
  ;;                       :pretty-print? true}}]}
  )
