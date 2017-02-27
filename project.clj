(defproject zark "1.0.0-SNAPSHOT"
  :description "Mighty ZarkOne!"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [[org.clojure/clojure "1.9.0-alpha14"]
   [com.datomic/datomic-free "0.9.5544" :exclusions [joda-time]]
   [org.clojure/core.logic "0.8.11"]
   [org.clojure/algo.monads "0.1.6"]

   [frankiesardo/tripod "0.2.0"] ; om-next example app
   [sablono "0.7.6"] ; hiccup style templating for om-next

   ;; webapp - begin
   [org.clojure/clojurescript "1.9.293" :exclusions [com.google.guava/guava]]
   [prismatic/om-tools "0.4.0"] ; more convenient dom elements
   [org.omcljs/om "1.0.0-alpha22" :exclusions [commons-codec]]
   ;; describe how a web server communicates with web apps
   ;; and how web apps can be chained together to process one request
   [ring "1.5.0" ;; low-level interface and library for web apps
    :exclusions [commons-codec]]
   [compojure "1.5.1"
    :exclusions [commons-codec org.clojure/tools.macro]]

   [com.andrewmcveigh/cljs-time "0.4.0"] ; (time/now)
   ;; webapp - end

   #_[org.clojure/tools.cli "0.3.5"] ; command line arguments

   ;; http://ianrumford.github.io/blog/2012/11/17/first-take-on-contracts-in-clojure/
   [org.clojure/core.contracts "0.0.6"]
   [org.clojure/core.memoize "0.5.9"]

   ;; [org.clojure/core.match "0.3.0-alpha4"] ; pattern matching library

   ;; quartzite dependency on slf4j-api should be auto-resolved
   ;; [org.slf4j/slf4j-nop "1.7.13"] ; Simple Logging Facade for Java
   [clojurewerkz/quartzite "2.0.0"] ; scheduling
   [clj-time "0.12.2"]
   ;; [korma "0.4.0"] ;; sql for clojure
   ;; [onelog "0.4.5"]  ;; used also by ring

   ;; [com.draines/postal "1.11.3"]            ; sending emails
   ;; [org.apache.commons/commons-email "1.4"] ; sending emails
   [clj-ssh "0.5.14"]
   [funcool/cuerdas "2.0.1"] ; string manipulation - (str/surround % "'")

   ;; (clojure.core.typed/check-ns) produces 'OutOfMemoryError PermGen space'
   [org.clojure/core.typed "0.3.28"]

   ;; [cauchy-jobs-kestrel "0.1.0"] ;; distributed message queue on the JVM

   ;; Laboratory for Type Theory Experiments
   [latte "0.3.7-SNAPSHOT"]
   ;; Formalization of (typed) Set theory in LaTTe.
   ;; [latte-sets "0.0.7-SNAPSHOT"] ; local installation: lein install
   ]
  :plugins
  [[lein-cljsbuild "1.1.3"]
   [lein-figwheel "0.5.8" :exclusions [org.clojure/clojure]]
   #_[refactor-nrepl "2.2.0"]
   #_[cider/cider-nrepl "0.11.0"]]
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
   ;; Load CIDER, refactor-nrepl and piggieback middleware
   :nrepl-middleware ["cider.nrepl/cider-middleware"
                      "refactor-nrepl.middleware/wrap-refactor"
                      "cemerick.piggieback/wrap-cljs-repl"]}

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
   :dev {:dependencies [[figwheel-sidecar "0.5.8"
                         :exclusions [com.google.guava/guava
                                      commons-codec
                                      org.clojure/tools.analyzer
                                      org.clojure/tools.analyzer.jvm]]
                        [org.clojure/test.check "0.9.0"] ; for clojure.spec
                        [com.cemerick/piggieback "0.2.1"]
                        [org.clojure/tools.nrepl "0.2.12"]]
         ;; Leads to Error loading cemerick.piggieback ... /queue-eval is not public
         ;; :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
         :source-paths ["src/cljs" "src/clj"]}})
