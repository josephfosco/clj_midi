(ns midi-async.aggregator)

;; (defn make-aggregator []
;;   (let [state (atom [])]
;;     (fn [byte]
;;       (let [b byte]
;;         (swap! state conj b)

;;         ;; simple 3-byte message assumption (MIDI note/control)
;;         (when (= 3 (count @state))
;;           (let [msg @state]
;;             (reset! state [])
;;             msg))))))


(defn make-aggregator []
  (let [state (atom [])]
    (fn [b]
      (swap! state conj b)

      ;; simple 3-byte messages (note on/off, CC)
      (when (= 3 (count @state))
        (let [msg @state]
          (reset! state [])
          msg)))))
