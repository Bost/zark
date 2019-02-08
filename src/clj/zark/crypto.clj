(ns zark.crypto
  "Some crypto stuff"
  (:require
   [clojure.tools.logging :as log]
   [utils.core :refer [in?]]
   )
  (:import java.security.MessageDigest))

(def just-chillin
  ["Is it getting better"
   "Or do you feel the same?"
   "Will it make it easier on you now?"
   "You got someone to blame"
   "You say one love, one life (One life)"
   "It's one need in the night"
   "One love (one love), get to share it"
   "Leaves you darling, if you don't care for it"
   "Did I disappoint you?"
   "Or leave a bad taste in your mouth?"
   "You act like you never had love"
   "And you want me to go without"
   "Well it's too late, tonight"
   "To drag the past out into the light"
   "We're one, but we're not the same"
   "We get to carry each other"
   "Carry each other"
   "One, one"
   ;; "One, one"
   ;; "One, one"
   ;; "One, one"
   "Have you come here for forgiveness?"
   "Have you come to raise the dead?"
   "Have you come here to play Jesus?"
   "To the lepers in your head"
   "Well, did I ask too much, more than a lot?"
   "You gave me nothing, now it's all I got"
   "We're one,..."])

(def hash-size "For demo reasons use just a portion of the hash strings" 4)
(def search-space-size (int (Math/pow 16 hash-size)))
(def workload-size (/ search-space-size
                      (int (Math/pow 16 (dec hash-size)))))
(def difficulty
  "How hard is to find a header for the block of transactions that their common
hash matches this regex"
  #"[0-3].*" #_#"[0-9a-f].*")

(def search-space
  "Create a list of all possible hashes (i.e. block headers)"
  (let []
    (map #(format (str "%0" hash-size "x") (biginteger %))
         (range search-space-size))))

(defn hash-fn [data]
  (let [algorith "md5" ;; "sha1" "sha-256" "sha-512"
        raw (.digest (MessageDigest/getInstance algorith)
                     (.getBytes (str data)))]
    (-> (format "%x" (BigInteger. 1 raw))
        (subs 0 hash-size))))

(defn transactions
  "Returns a bunch of example txs to work with"
  []
  (->> (range (+ 4 (rand-int 0)))
       (map (comp
             (fn [x]
               {:src (subs x 0 (/ hash-size 2))
                :sum (format "%.2f" (* 10 (rand)))
                :dst (subs x (/ hash-size 2) hash-size)})
             hash-fn
             rand))))

(defn nth-workload
  "Return nth portion of the search-space"
  [n]
  (nth (partition workload-size search-space) n))

(defn create-block
  "Put a coll of transactions txs in a container (i.e. block) with some header
and calculate the hash value of this block"
  [txs header]
  #_(Thread/sleep (* (+ 3 (rand-int 4)) 100))
  (let [block {:txs txs :header header}]
    (conj block
          {:b-hash (hash-fn block)})))

(defn search [{:keys [b-hash header] :as block}]
  "Achtung baby, this is expensive! https://youtu.be/ujKF85eMWoo"
  (if-let [valid (boolean (re-matches difficulty b-hash))]
    (conj block
          {:valid valid})
    (str "header '" header "' isn't valid"
         #_(str "... " (rand-nth just-chillin)))))

(defn computation []
  (let [start (. System (nanoTime))]
    [(mine-coin)
     (str "Mined in "
          (/ (double (- (. System (nanoTime)) start)) 1000000.0) " msecs"
          ".... "
          #_(rand-nth just-chillin))]))

(defn miner [workload]
  (Thread/sleep (* 1 (+ 3 (rand-int 4)) 100))
  (map (fn [header]
         (search
          (create-block txs header)))
       workload))

(defn miner-f [workload] (miner workload))
(defn miner-g [workload] (miner workload))
(defn miner-h [workload] (miner workload))
(defn miner-i [workload] (miner workload))

(defn coordinate []
  (let [miners [miner-f miner-g miner-h miner-i]
        search-results (pmap (fn [miner idx]
                               (miner (nth-workload idx)))
                             miners
                             (range))
        #_[false false false true false]
        ]
    (println "search-results" search-results)
    (if (in? search-results true)
      "stop-search"
      "continue-search")))

