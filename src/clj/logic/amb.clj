(ns logic.amb
  "Jonathan Bartlett: Continuations: The Swiss Army Knife of Flow Control
  https://youtu.be/Ju3KKu_mthg?t=1698"
  (:use clojure.contrib.monads))

(defn amb
  "Can be implemented using continuation"
  [wss]
  (let [valid-word (fn [w1 w2]
                     ;; last letter of the word w1 must be the same as the first
                     ;; letter of w2
                     (if (and w1 (= (last w1) (first w2)))
                       (str w1 " " w2)))]
    (filter #(reduce valid-word %)
            (with-monad sequence-m (m-seq wss)))))

#_(amb
   '(("the" "that" "a")
     ("frog" "elephant" "thing")
     ("walked" "treaded" "grows")
     ("slowly" "quickly")))
;; => (("that" "thing" "grows" "slowly"))
