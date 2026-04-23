(ns midi-async.engine
  (:require [clojure.core.async :as async]
            [midi-async.parser :as p]))

(defonce start-time (atom nil))

(defn start-engine [in-ch]

  (reset! start-time (System/currentTimeMillis))
  (println "Engine started at" @start-time)

  (let [parser (p/make-parser)]

    (async/go-loop []
      (when-let [b (async/<! in-ch)]

        (when-let [msg (parser b)]
          (let [event (assoc (p/decode msg)
                             :timestamp (- (System/currentTimeMillis)
                                           @start-time))]
            (println "MIDI EVENT:" event)))

        (recur)))))
