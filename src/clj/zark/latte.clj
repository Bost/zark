(ns zark.latte
  "This is a talk about LaTTe given @ Euroclojure 2016."
   ;; These belong to logic ;-)
  (:refer-clojure :exclude [and or not])
  ;; LaTTe core and main top-level forms
  (:require [latte.core :as latte
             :refer [definition defthm defaxiom defnotation
                     forall lambda ==>
                     assume have proof try-proof
                     term type-of type-check?]]

            ;; ... the "standard" library (propositions, quantifiers and equality)
            [latte.prop :as p :refer [<=> and or not]]
            [latte.quant :as q :refer [exists]]
            [latte.equal :as eq :refer [equal]]))


;; see latte.kernel.presyntax (def +reserved-symbols+ '#{□ ✳ λ Π ⟶ ∃ ∀})
(term
 (λ [A :type] (λ [x A] x))
 ;; ^^^ (fn [x] x) in LaTTe ^^^
 )

(type-check?
 ;; the lambda-term:
 (λ [A :type]
    (λ [x A] x))
 ;; is of type ...
 (∀ [A :type]
  (==> A A)))

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
  [[A :type] [B :type]]
  (==> A B
       (and- A B)))

(proof and-intro-
    :term
  (λ [x A]
    (λ [y B]
       (λ [C :type]
          (λ [f (==> A B C)]
             ((f x) y))))))

(defthm and-elim-left- ""
  [[A :type] [B :type]]
  (==> (and- A B)
       A))

(proof and-elim-left-
    :script
  "Our hypothesis"
  (assume [p (and- A B)]
    "The starting point: use the definition of conjunction:
             (∀ [C :type]
                (==> (==> A B C)
                     C))"
    (have <a> (==> (==> A B A) A) :by (p A))
    "We need to prove that if A is true and B is true then A is true"
    (assume [x A
             y B]
      (have <b> A :by x)
      (have <c> (==> A B A) :discharge [x y <b>])) ;; (λ [x A] (λ [x B] x))
    "Now we can use <a> as a function"
    (have <d> A :by (<a> <c>))
    (qed <d>)))