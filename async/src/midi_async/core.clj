(ns midi-async.core
  (:require [midi-async.midi :as midi]
            [midi-async.engine :as engine]
            [clojure.core.async :as async]))

(defonce system (atom nil))

;; (defn start! []
;;   (println "Starting MIDI system...")

;;   (let [proc (.exec (Runtime/getRuntime)
;;                     (into-array String ["amidi" "-p" "hw:0,0,0" "-d"]))

;;         bytes (midi/start-midi-stream proc)]

;;     (engine/start-engine bytes)
;;     (reset! system {:proc proc})
;;     (println "System running")))


(defn start! []
  (println "Starting MIDI system...")

  (let [proc (.exec (Runtime/getRuntime)
                    "amidi -p hw:0,0,0 -d")

        bytes (midi/start-midi-stream proc)]

    (engine/start-engine bytes)

    (reset! system {:proc proc})
    (println "System running")))
