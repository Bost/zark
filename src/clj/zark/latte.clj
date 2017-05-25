(ns zark.latte
  "This is a talk about LaTTe given @ Euroclojure 2016."
  ;; These belong to logic ;-)
  (:refer-clojure :exclude [and or not])
  ;; LaTTe core and main top-level forms
  (:require [latte.core :as latte
             :refer [definition defthm defaxiom defnotation
                     forall lambda ==>
                     assume have proof try-proof
                     term type-of type-check? qed]]

            ;; ... the "standard" library (propositions, quantifiers and equality)
            [latte.prop :as p :refer [<=> and or not]]
            [latte.quant :as q :refer [exists]]
            [latte.equal :as eq :refer [equal]]

            [clojure.core :as c]
            ;; [clojure.set :as set]
            [clojure.walk :as walk]
            [clojure.spec.gen :as gen]
            [clojure.spec.test :as stest] ;; for instrumentation
            [clojure.spec :as s]))

;; see latte.kernel.presyntax (def +reserved-symbols+ '#{□ ✳ λ Π ⟶ ∃ ∀})
;; ✳ - type of types (lambda term); type of ✳ is □; ✳ : □
;; □ - kind (in latte noted as ':type'); the kind ifself has no type
(term
 (λ [A :type] (λ [x A] x))
 ;; ^^^ (fn [x] x) in LaTTe ^^^
 )

(type-check?
 ;; the lambda-term:
 (λ [A :type] (λ [x A] x))
 ;; is of type ...
 (∀ [A :type] (==> A A)))

(type-check?
 ;; the lambda-term:
 (λ [A B C :type]
    (λ [f (==> A B)]
       (λ [g (==> B C)]
          (λ [x A]
             (g (f x))))))

 ;; is of type ...
 (∀ [A B C :type]
  (==> (==> A B)  ;; (==> X Y Z) ≡ (==> X (==> Y Z))
       (==> B C)
       (==> A C))))

(definition and-  ;; nameclash!
  "Conjunction in Type Theory"
  [[A :type] [B :type]]
  (∀ [C :type]
   (==> (==> A B C)
        C)))

(defthm and-intro- ""
  [[A :type] [B :type]] (==> A B (and- A B)))

(proof and-intro- :term
  (λ [x A]
    (λ [y B]
      (λ [C :type]
        (λ [f (==> A B C)]
          ((f x) y))))))

(defthm and-elim-left- ""
  [[A :type] [B :type]] (==> (and- A B) A))

#_(proof and-elim-left- :script
  ;; "Our hypothesis"
  (assume [p (and- A B)]
    ;; "The starting point: use the definition of conjunction:
    ;;          (∀ [C :type]
    ;;             (==> (==> A B C)
    ;;                  C))"
    (have <f> (==> (==> A B A) A) :by (p A))
    ;; "We need to prove that if A is true and B is true then A is true"
    (assume [x A
             y B]
      (have <a> A :by x)
      (have <c> (==> A B A) :discharge [x y <a>])) ;; (λ [x A] (λ [y B] x))
    ;; "Now we can use <f> as a function"
    (have <d> A :by (<f> <c>))
    (qed <d>)))

(defthm and-elim-right- ""
  [[A :type] [B :type]] (==> (and- A B) B))

#_(proof and-elim-right- :script
  "Our hypothesis"
  ;; i.e assume A, B are true and p is the witness; i.e I suppose I have
  ;; a lambda term to prove
  (assume [p (and- A B)]
    "The starting point: use the definition of conjunction:
             (∀ [C :type]
                (==> (==> A B C)
                     C))"
    (have <a> (==> (==> A B B) B) :by (p B))
    "We need to prove that if A is true and B is true then B is true"
    (assume [x A
             y B]
      (have <b> B :by y) ; 'have' is a construct to make a proof step
      ;; :by is followed by a lambda temp
      ;; :discharge internally creates a lambda term (λ [x A] (λ [y B] <b>))
      ;; <b> is in fact x
      (have <bb> (==> A B B) :discharge [x y <b>])) ;; (λ [x A] (λ [y B] y))
    "Now we can use <a> as a function"
    (have <d> B :by (<a> <bb>))
    (qed <d>)))

(defthm impl-refl "Implication is reflexive."
  [[A :type]] (==> A A))

(proof impl-refl :term (λ [x A] x))

(proof impl-refl :script
  "assuming A holds, as an hypothesis named x we can deduce A by x hence A
  implies A as stated (QED)."
  (assume [x A]
    (have concl A :by x) ;; internally (λ [x A] x) is created
    (qed concl)))


(defthm impl-ignore "A variant of reflexivity."
  [[A :type] [B :type]] (==> A B A))

(proof impl-ignore :term (λ [x A] (λ [y B] x)))

(proof impl-ignore :script
  (assume [
           x A
           y B
           xx A
           ]
    (have <a> A :by (H1 x))
    (qed <a>)))


(defthm impl-a [[A :type] [B :type]] (==> A B A))
(proof impl-a :term (λ [x A] (λ [y B] x)))
(defthm impl-b [[A :type] [B :type]] (==> A B B))
(proof impl-b :term (λ [x A] (λ [y B] y)))

(defthm impl-i [[A :type] [B :type]] (==> A B))
(proof impl-i
    :script
  (assume [x A
           B :type
           z (==> A B)]
    (have b B :by (z x))
    (qed b)))


(defthm and-intro
  "Introduction rule for logical conjunction."
  [[A :type] [B :type]]
  (==> A B
       (and B A)))

(proof and-intro
       :term
       (λ [x A]
          (λ [y B]
             (λ [C :type]
                (λ [z (==> B A C)]
                   z y x)))))

(proof and-intro
    :script
  (assume [x A
           y B
           C :type
           z (==> B A C)]
    (have a (==> A C) :by (z y))
    (have b C :by ((a) x))
    (qed b)))

(defthm modus-ponens "Implication elimination"
  [[A :type] [B :type]] (==> (==> A B) A B))

(proof modus-ponens :term (λ [f (==> A B)] (λ [x A] (f x))))

(term (λ [A B :type] (λ [f (==> A B)] (λ [x A] (f x)))))
#_
(λ [A ✳]
  (λ [B ✳]
    (λ [f (Π [⇧ A] B)] (λ [x A] [f x]))))

(term (∀ [A B :type] (==> (==> A B) A B)))
#_
(Π [A ✳]
  (Π [B ✳]
    (Π [⇧ (Π [⇧ A] B)] (Π [⇧ A] B))))

(type-check?
  (λ [A B :type] (λ [f (==> A B)] (λ [x A] (f x))))
  (∀ [A B :type] (==> (==> A B) A B)))

(proof modus-ponens :script
  (assume [Hypothesis (==> A B)
           x A]
    (have concl B :by (Hypothesis x))
    (qed concl)))

;; see defthm or-not-impl-elim
(defthm modus-tollens "Denying the consequent"
  [[A :type] [B :type]] (==> (==> A B) (not A) (not B)))

(term (∀ [A B :type] (==> (==> A B) (not A) (not B))))
#_
(Π [A ✳]
  (Π [B ✳]
    (Π [⇧ (Π [⇧ A] B)]
      (Π [⇧ (#'latte.prop/not A)]
        (#'latte.prop/not B)))))

(term (λ [A B :type] (λ [f (==> A B)] (λ [x (not A)] x))))
#_
(λ [A ✳]
  (λ [B ✳]
    (λ [f (Π [⇧ A] B)]
      (λ [x (#'latte.prop/not A)] x))))
#_
(proof modus-tollens :term ...)

#_
(proof modus-tollens :script ...)

;; https://github.com/gigasquid/genetic-programming-spec
(defn score [creature test-data]
  (try
    (let [problems (:clojure.spec/problems
                    (s/explain-data (eval (:program creature)) test-data))]
      (if problems
        (assoc creature :score (get-in problems [0 :in 0]))
        (assoc creature :score 100)))
    (catch Throwable e (assoc creature :score 0))))

(def preds ['integer? 'string? 'boolean? '(s/and integer? even?) '(s/and integer? odd?)])
(def seqs ['s/+ 's/*])
(def and-ors ['s/and 's/or])

(def seq-prob 0.3)
(def nest-prob 0.00)
(def max-depth 4)
(def and-or-prob 0.85)

(declare make-random-arg) ; forward declaration

(defn make-random-seq [n]
  (cond
    (< (rand) nest-prob)
    `(s/spec (~(rand-nth seqs) ~(make-random-arg (dec n))))

    (< (rand) and-or-prob)
    `(~(rand-nth and-ors) ~(make-random-arg (dec n)) ~(make-random-arg (dec n)))

    :else
    `(~(rand-nth seqs) ~(make-random-arg (dec n)))))

(defn make-random-arg [n]
  (if (c/and (pos? n) (< (rand) seq-prob))
    (make-random-seq n)
    (rand-nth preds)))

(defn make-random-cat [len]
  (let [args (reduce (fn [r i]
                       (conj r (keyword (str i))
                             (make-random-arg max-depth)))
                     []
                     (range len))]
    `(s/cat ~@args)))

(defn initial-population [popsize max-cat-length]
  (for [i (range popsize)]
    {:program (make-random-cat (inc (rand-int max-cat-length)))}))

(defn mutable? [node]
  (c/or (when (seq? node)
        (contains? (clojure.set/union (set seqs) #{'clojure.spec/spec}) (first node)))
      (contains? (set preds) node)))
(def mutate-prob 0.1)

(defn mutate
  "More often than not it generates the same over and over again"
  [creature]
  (let [program (:program creature)
        mutated-program (walk/postwalk
                         (fn [x] (if (c/and (mutable? x) (< (rand) mutate-prob))
                                   (make-random-arg max-depth)
                                   x)) program)]
    (assoc creature :program mutated-program)))

#_(mutate {:program '(clojure.spec/cat :0 (s/and integer? odd?) :1 integer?)})

(def crossover-prob 0.7)

(defn crossover [creature1 creature2]
  (let [program1 (:program creature1)
        program2 (:program creature2)
        chosen-node (first (walk/walk
                            #(when
                                 (c/and (< (rand) crossover-prob)
                                      (mutable? %))
                               %)
                            #(remove nil? %) program1))
        crossed-over? (atom false)
        crossover-program (if chosen-node
                            (walk/postwalk
                             (fn [x]
                               (if (c/and (mutable? x)
                                        (< (rand) crossover-prob)
                                        (c/not @crossed-over?))
                                 (do (reset! crossed-over? true) chosen-node)
                                 x))
                             program2)
                            program2)]
    {:program crossover-program}))

#_(crossover {:program '(clojure.spec/cat :0 (s/and integer? odd?) :1 integer?)}
           {:program '(clojure.spec/cat :0 string? :1 boolean?)})


(defn select-best [creatures tournament-size]
  (let [selected (repeatedly tournament-size #(rand-nth creatures))]
    (-> (sort-by :score selected) reverse first)))

(defn perfect-fit [creatures]
  (first (filter #(= 100 (:score %)) creatures)))

(def new-node-prob 0.05)
(def max-depth 4)

(defn evolve [pop-size max-gen tournament-size test-data]
  (loop [n max-gen
         creatures (initial-population pop-size (count test-data))]
    (println "generation " (- max-gen n))
    (let [scored-creatures (map (fn [creature] (score creature test-data)) creatures)]
     (if (c/or (zero? n) (perfect-fit scored-creatures))
       scored-creatures
       (let [elites (take 2 (reverse (sort-by :score scored-creatures)))
             new-creatures (for [i (range (- (count creatures) 2))]
                             ;; add a random node to improve diversity
                             (if (< (rand) new-node-prob)
                               {:program (make-random-cat (count test-data))}
                               (let [creature1 (select-best scored-creatures tournament-size)
                                     creature2 (select-best scored-creatures tournament-size)]
                                 (mutate (crossover creature1 creature2)))))]
         (println "best-scores" (map :score elites))
         (recur (dec n) (into new-creatures elites)))))))

;; (def creature-specs (evolve 100 100 7 ["hi" true 5 10 "boo"]))

#_(perfect-fit creature-specs)

;; runs very long
#_(s/exercise (eval (:program (perfect-fit creature-specs))) 5)

(s/def ::proof-type (s/and vector?
                           (fn [v] (= :qed (first v)))
                           (fn [v] (symbol? (second v)))))

(defn construct-pterm
  "Construct proof definition with its proof-term for a given theorem.
  Example: (construct-pterm 'impl-refl)
  => (proof impl-refl :term (lambda [x A] x))"
  [theorem-name]
  (list 'proof theorem-name ':term
        ;; TODO construct the proof term
        '(lambda [x A] x)))

(s/explain-data (s/cat :0 ::proof-type) [(eval (construct-pterm 'impl-refl))])
(score {:program '(s/cat :0 ::proof-type) :score 0}
       [(eval (construct-pterm 'impl-refl))])
