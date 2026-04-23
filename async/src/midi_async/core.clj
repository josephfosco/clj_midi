(ns midi-async.core
  (:require [midi-async.midi :as midi]
            [midi-async.engine :as engine]
            [midi-async.aggregator :as agg]
            [clojure.core.async :as async]))

(defonce system (atom nil))

(defn start! []
  (println "Starting raw MIDI stream...")


;;  (let [bytes (midi/start-midi-stream "/dev/snd/midiC0D0")
  (let [bytes (midi/start-midi-stream "hw:0,0,0")
        aggregate (agg/make-aggregator)
        out (async/chan 100)]

    (async/go-loop []
      (when-let [b (async/<! bytes)]
        (when-let [msg (aggregate b)]
          (async/>! out msg))
        (recur)))

    (engine/start-engine out)
    (reset! system {:in bytes :out out})
    (println "Running")))
