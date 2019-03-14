(ns zark.q-learning
  "
  clojureD 2019: 'Reinforcement Learning Made Simple' by Robert Avram
  https://youtu.be/6SDCwn8MGtQ
  Tabular Q-learning with greedy Goodnes epsilon
  See also https://github.com/AvramRobert/periculum.git

  Reinforced Learning of an agent
  Reward / Punish behavior results
  Problems of optimality

  Spaces - 5 quantities:
  1. State S - starting State S0
  2. Action: S -> List A
  3. Reward: (S, A) -> R (eval funcion)
  4. Transition: (S, A) -> S
  5. Terminal States: S -> Boolean

  Q-learning: episodes = iterations
  Q values - agents experience: an hash-map
  { s0 a1 -> E(Rs0, a1) }
  - from Reward compute a Goodnes epsilon of an action (type of double)

  Policy - for picking an action in a given state:
  policy: (s0, actions(s0), Qs') -> A
  Policy as a probability - action changes  / doesn't change the state
  ")

(defn max-by [f coll]
  (let [maximum (->> coll (map f) (apply max))
        value (find-some #(= (f %) maximum) coll)]
    (if (map? coll)
      (apply hash-map value)
      value)))

(defn q-learning
  "
  Number of iterations: episodes
  Discount factor: gamma
  Learning grade: alpha
  Starting state: s0
  "
  [:keys [episodes gamma alpha s0 terminal?
          actions transition reward]]
  (let [Qs {}] ;; initialize all the states and actions in the Q-values
    ;; reduce, i.e. update the experience, i.e. Q-values in every iteration
    (reduce
     (fn [Qs episode]
       (println "episode")
       (loop [S s0
              Qs Qs]
         (if (not (terminal? S))
           (let [A  (policy S (actions S) Qs)
                 R  (reward S A)
                 S' (transition S A)
                 A' (or
                     (;; short-circuit out in the case of nil, i.e. at the
                      ;; begining when the experience map is empty
                      some->>
                      ;; in the experience map: look at the actions at that
                      ;; state
                      (Qs S')
                      ;; max by the action values
                      (max-by val)
                      ;; entry points of the experience map
                      (keys)
                      ;; fst entry point in the experience map, i.e. the
                      ;; greediest action the agent knows about at this point in
                      ;; time
                      (first))
                     ;; ... and if you've never done it before, just do
                     ;; something...
                     (rand-nth (actions S')))
                 ;; calculate the a experience and update it
                 Qs (let [Q-SA (get-in Qs [S A]
                                       ;; if I don't know anything about then just
                                       ;; return 0
                                       0)
                          Q-S'A' (get-in Qs [S' A']
                                         ;; if I don't know anything about then just
                                         ;; return 0
                                         0)
                          Q-SA (+ Q-SA
                                  (* alpha (+ R
                                              (- (* gamma Q-S'A')
                                                 Q-SA))))
                          ;; ... at this state for this action replace the
                          ;; experience-value with what ever is calculated
                          (assoc-in Qs [S A] Q-SA)])]
             (recur S' Qs)))))
     Qs
     ;; from 0 to n
     (range 0 episodes))))

;; derive the optimal path, based on the experience the agent has accumulated
(defn solution [Qs {:keys [s0 transition terminal]}]
  (loop [S s0
         optimum []]
    (if (not (terminal S))
      (let [A (some->> (Qs S)
                       (max-by val)
                       (keys)
                       (first))
            S' (transition S A)]
        (recur S' (conj optimum {:state S :action A})))
      optimum)))

#_
(def data
  {:episodes 300
   :transition transitions
   :reward rewards
   :actions actions
   :terminal terminal
   :s0 s0
   :gamma 1.0
   :alpha 0.3
   ;; 70% probability of `explore the action states`, 30% probability of `do as before`
   :policy (e-greedy 0.7)})
