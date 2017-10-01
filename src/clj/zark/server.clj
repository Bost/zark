(ns ^{:doc "Simple http server: http://localhost:8080/"}
    zark.server
  (:use ring.adapter.jetty))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello World"})

(run-jetty handler {:port 8080})
