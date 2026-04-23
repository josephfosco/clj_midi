(ns midi-async.engine
  (:require [clojure.core.async :as async]
            [midi-async.parser :as p]))

(defn start-engine [in-ch]
  (let [parser (p/make-parser)]

    ;; (async/go-loop []
    ;;   (when-let [b (async/<! in-ch)]
    ;;     (when-let [msg (parser b)]
    ;;       (println "MIDI EVENT:" (p/decode msg)))
    ;;     (recur))))

    (async/go-loop []
      (when-let [b (async/<! in-ch)]

;;        ;; DEBUG LINE 👇
;;        (println "RAW BYTE" b)

        (when-let [msg (parser b)]
          (println "MIDI EVENT:" (p/decode msg)))

        (recur)))
  ))
