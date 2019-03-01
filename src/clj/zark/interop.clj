(ns zark.interop
  (:require
   [midje.sweet :refer :all]
   [clojure-python.core :as base])
  (:import [org.domain Main]))

;; doesn't work
#_(with-test-interp
  (base/py-import-lib example)
  (base/import-fn example hello)
  (hello "world"))

(def class-name "org.domain.Main")
(clojure.lang.Reflector/getStaticField class-name "FIELD")
;; => Main.class: public static String FIELD = "..."
(type (Class/forName class-name))
;; => java.lang.Class
(type (.newInstance (Class/forName class-name)))
;; => org.domain.Main
(instance? (Class/forName class-name) (.newInstance (Class/forName class-name)))
;; => true
(.instanceFn1 (new org.domain.Main) "foo")
;; => instanceFn0: arg0: foo

;; compiler emit warnings when reflection is needed to resolve Java method calls or field accesses
;; (set! *warn-on-reflection* true)
(set! *warn-on-reflection* false)

(.instanceFn1 (.newInstance (Class/forName class-name)) "foo")
;; => instanceFn0: arg0: foo

(clojure.lang.Reflector/invokeStaticMethod class-name "staticFn0" (to-array nil))
;; => staticFn0: no-args

(clojure.lang.Reflector/invokeInstanceMethod
 (.newInstance (Class/forName class-name))
 "instanceFn1"
 (to-array ["foo"]))
;; => instanceFn0: arg0: foo

(defn get-static-fn [^Class class method]
  (fn [& args]
    (clojure.lang.Reflector/invokeStaticMethod
     (.getName class)
     (str method)
     (to-array args))))

((get-static-fn org.domain.Main "staticFn0"))
;; => staticFn0: no-args

((get-static-fn org.domain.Main "staticFn1") "foo")
;; => staticFn1: arg0: foo

((get-static-fn org.domain.Main "staticFn2") "foo" "bar")
;; => staticFn1: arg0: foo, arg1: bar

(defn get-instance-fn [^Class class method]
  (fn [& args]
    (clojure.lang.Reflector/invokeInstanceMethod
     (.newInstance class)
     (str method)
     (to-array args))))

((get-instance-fn org.domain.Main "instanceFn0"))
;; => instanceFn0: no-args

((get-instance-fn org.domain.Main "instanceFn1") "foo")
;; => instanceFn0: arg0: foo

((get-instance-fn org.domain.Main "instanceFn2") "foo" "bar")
;; => instanceFn0: arg0: foo, arg1: bar

((get-static-fn org.domain.Main "main") (into-array String ["aaa" "bbb"]))
;; => main: args: aaa, bbb
;; => nil
