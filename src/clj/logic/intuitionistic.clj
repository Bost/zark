(ns logic.intuitionistic
  (:refer-clojure :exclude [id and or not int =])
  (:require
   [latte.core :as latte
    :refer [
            definition defthm defaxiom defnotation
            forall lambda
            ;; ==> this does not exist
            assume have pose proof lambda forall
            term type-of type-check?
            ]]))

(term
 (λ [A :type] (λ [x A] x)))

;; TODO a difference between an axiom and definition

;; definition
;; defaxiom
;; defimplicit
;; (defthm     name "declare a theorem"   [[prm-1 type-1] .. [prm-n type-n]] theorem-proposition)
;; (defaxiom   name "declare an axiom"    [[prm-1 type-1] .. [prm-n type-n]] axiom-statement)
;; (definition name "defines a math-term" [params] λ-term)
;; (deflemma   name "declare an auxiliary theorem")

(defaxiom id- ""
  [[A :type]]
  A)



