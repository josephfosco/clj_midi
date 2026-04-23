(ns midi-async.midi
  (:require [clojure.core.async :as async]))

(defn start-midi-stream [proc]
  (let [out (async/chan 100)
        reader (clojure.java.io/reader (.getInputStream proc))]

    (async/thread
      (doseq [line (line-seq reader)]
        (println "RAW LINE:" line)

        (let [bytes (->> (clojure.string/split line #"\s+")
                         (remove clojure.string/blank?)
                         (map #(Integer/parseInt % 16)))]

          (doseq [b bytes]
            (println "RAW BYTE:" b)
            (async/>!! out b)))))

    out))
