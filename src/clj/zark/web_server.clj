(ns zark.web-server
  "Simple http server: http://localhost:8080/"
  (:require [ring.adapter.jetty :as jetty]
            [clojure.data.json :as json]))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body
   (json/write-str {:chat_id "112885364" :text "Hello from zark.web-server"})
   #_(json/write-str "Hello from zark.web-server")})

(defonce server (jetty/run-jetty handler {:port 8080 :join? false}))
(defn start [] (.start server))
(defn stop [] (.stop server))
