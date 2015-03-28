(ns zark.ycombinator)

"Print evaluated expression and return its res"
(defmacro dbg [x]
  `(let [x# ~x]
     (println '~x "=" x#) x#
     ))

(defn Y [f]
  ((fn [x]
     (f (fn [arg]
          ((x x) (dbg arg)))))   ; this dbg returns 5
   (fn [x]
     (f (fn [arg]
          ((x x) (dbg arg))))))) ; this dbg returns 4, 3, 2, 1, 0

(defn factorial [n]
  (if (zero? n)
    1
    (* n (factorial (dec n)))))


; We want to define an anonymous function which we want to apply to itself
; and we want to start this function with n = 6
;(
      ;(fn [n]
        ;(if (zero? n)
          ;1
          ;(* n (?? (dec n)))))
       ;6
;)

((Y (fn [rec]
      (fn [n]
        (if (zero? n)
          1
          (* n (rec (dec n)))))))
 6)
