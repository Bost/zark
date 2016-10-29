(ns zark.spec
  (:require [clojure.spec :as s]))

;; keyword beginning with two colons is resolved in the current namespace
;; i.e. ::rect is read as :zark.spec/rect

;; cat - concatenation of predicates/patterns
;; alt - choice among alternative predicates/patterns
;; * - 0 or more of a predicate/pattern
;; + - 1 or more of a predicate/pattern
;; ? - 0 or 1 of a predicate/pattern


(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email-type (s/and string? #(re-matches email-regex %)))

(s/def ::acctid int?)
(s/def ::first-name string?)
(s/def ::last-name string?)
(s/def ::email ::email-type)

(s/def ::person (s/keys :req [::first-name ::last-name ::email]
                        :opt [::phone]))
(defn person-name
  [person]
  {:pre [(s/valid? ::person person)]
   :post [(s/valid? string? %)]}
  (str (::first-name person) " " (::last-name person)))

;; (person-name 42)
;;=> java.lang.AssertionError: Assert failed: (s/valid? :my.domain/person person)

(person-name {::first-name "Elon" ::last-name "Musk" ::email "elon@example.com"})
;;=> "Elon Musk"


(defn person-name2
  [person]
  (let [p (s/assert ::person person)]
    (str (::first-name p) " " (::last-name p))))

;; (s/check-asserts true) ;;  By default assertion checking is off
;; (person-name2 100) ;;=> <lenghty error>

(person-name2 {::first-name "Elon" ::last-name "Musk" ::email "elon@example.com"})
;;=> "Elon Musk"



(s/def ::config (s/*
                 (s/cat :prop string?
                        :val  (s/alt :s string? :b boolean?))))

(s/conform ::config ["-server" "foo" "-verbose" true "-user" "joe"])

(defn- set-config [prop val]
  ;; dummy fn
  (println "set" prop val))

(defn configure [input]
  (let [parsed (s/conform ::config input)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data ::config input)))
      (for [{prop :prop [_ val] :val} parsed]
        (set-config (subs prop 1) val)))))

(configure ["-server" "foo" "-verbose" true "-user" "joe"])

(s/describe ::config)
;;=> (* (cat :prop string? :val (alt :s string? :b boolean?)))


(defn ranged-rand
  "Returns random int in range start <= rand < end"
  [start end]
  (+ 10 start (long (rand (- end start)))))

(s/fdef ranged-rand
        :args (s/and (s/cat :start int? :end int?)
                     #(< (:start %) (:end %)))
        :ret int?
        :fn (s/and #(>= (:ret %) (-> % :args :start))
                   #(< (:ret %) (-> % :args :end))))

(ranged-rand 3 5)


(defn adder [x] #(+ x %))

(s/fdef adder
        :args (s/cat :x number?)
        :ret (s/fspec :args (s/cat :y number?)
                      :ret number?)
        :fn #(= (-> % :args :x) ((:ret %) 0)))
