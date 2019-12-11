;; M-x run-scheme
(define j-bob-home "~/dev/j-bob/scheme/")

;; Load the J-Bob language:
(load (string-append j-bob-home "j-bob-lang.scm"))
;; Load J-Bob, our little proof assistant:
(load (string-append j-bob-home "j-bob.scm"))
;; Load the transcript of all proofs in the book:
(load (string-append j-bob-home "little-prover.scm"))
;; Run every proof in the book, up to and including the proof of align/align:
(dethm.align/align)
