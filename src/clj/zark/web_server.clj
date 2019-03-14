(ns zark.web-server
  "Simple http server: http://localhost:8080/"
  (:use [ring.adapter.jetty]))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello World"})

(run-jetty handler {:port 8080})
