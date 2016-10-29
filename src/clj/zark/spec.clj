(ns zark.spec
  (:require [clojure.spec.gen :as gen]
            [clojure.spec.test :as stest] ;; for instrumentation
            [clojure.spec :as s]))

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
  (+ start (long (rand (- end start)))))

(s/fdef ranged-rand
        :args (s/and (s/cat :start int? :end int?)
                     #(< (:start %) (:end %)))
        :ret int?
        :fn (s/and #(>= (:ret %) (-> % :args :start))
                   #(< (:ret %) (-> % :args :end))))

(ranged-rand 3 5) ;;=> 13
;; (ranged-rand 5 3) ;;=> Error

(defn adder [x] #(+ x %))

(s/fdef adder
        :args (s/cat :x number?)
        :ret (s/fspec :args (s/cat :y number?)
                      :ret number?)
        :fn #(= (-> % :args :x) ((:ret %) 0)))


(gen/generate (s/gen int?)) ;;=> 311

;; s/and order of arguments is important:
(gen/generate (s/gen (s/and int? even?))) ;;=> -80
;; (gen/generate (s/gen (s/and even? int?))) ;;=> Unable to construct gen at: [] for: even?

;; TODO take a look at how to create custom generators


(s/exercise-fn `ranged-rand)
;; =>
;; ([(-2 -1)   -2]
;;  [(-3 3)     0]
;;  [(0 1)      0]
;;  [(-8 -7)   -8]
;;  [(3 13)     7]
;;  [(-1 0)    -1]
;;  [(-69 99) -41]
;;  [(-19 -1)  -5]
;;  [(-1 1)    -1]
;;  [(0 65)     7])


;; Instrumentation validates that the :args spec is being invoked on
;; instrumented fns and thus provides validation for external uses of a fn.
(stest/instrument `ranged-rand) ;;=> [zark.spec/ranged-rand]
(ranged-rand 3 5) ;;=> 13
;; (ranged-rand 5 3) ;;=> Error

#_(defn ranged-rand  ;; BROKEN!
  "Returns random int in range start <= rand < end"
  [start end]
  (+ start (long (rand (- start end)))))

(stest/abbrev-result (first (stest/check `ranged-rand)))
