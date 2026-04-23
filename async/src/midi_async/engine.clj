(ns midi-async.engine
  (:require [clojure.core.async :as async]
            [midi-async.decoder :as dec]))

(defn start-engine [ch]
  (async/go-loop []
    (when-let [msg (async/<! ch)]
      (println "EVENT:" (dec/decode msg))
      (recur))))
