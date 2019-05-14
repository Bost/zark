(ns zark.clojured
  (:require [halfling.task :as t]))

(def adding (t/task (+ 1 1)))

(t/executed? adding)

(t/run adding)


(def no-please (t/task
                (Thread/sleep
                 ;; 400
                 Integer/MAX_VALUE
                 )
                42))

(t/executed? no-please)

(t/run-async no-please)

(t/get! (t/run adding))

@(t/run adding)

@(t/run-async no-please)

(defn my [x]
  (let [r (inc x)]
    (println "r" r)
    r))

(def cnt (atom 0))

(def crucial-maths
  (do
    (reset! cnt 0)
    (while (< @cnt 10)
      (->
       (t/task
        (do
          (println "my" (my 0))
          (Thread/sleep 400)
          (swap! cnt inc)
          (println "cnt" @cnt)))))))


(defn my [x]
  (let [r (inc x)]
    (println "rrrr" r)
    r))

;; @(t/run-async crucial-maths)

;; (t/run-async crucial-maths)

;; (t/executed? crucial-maths)

;; (t/broken? crucial-maths)

;; (t/get! (t/run-async crucial-maths))

;; clojureD 2019: Robert Avram https://youtu.be/Ekngl04XcdU?t=857
(defrecord Task (fns result))
#_(defmacro task [body]
  ...)
