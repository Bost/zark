((nil
  .
  ((eval
    .
    (progn
      (let ((local-map (or (current-local-map) (make-keymap))))
        (use-local-map local-map)

        (setq ufo-dir "~/dev/zark")

        (define-key local-map (kbd "<s-f2>")
          (lambda ()
            (interactive)
            (find-file (concat ufo-dir "/src/zark/reasoned-schemer.clj"))))))))))
