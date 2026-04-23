(ns midi-async.midi
  (:require [clojure.core.async :as async])
  (:import [java.io FileInputStream]))

;; (defn start-midi-stream [dev-path]
;;   (let [out (async/chan 100)
;;         in (FileInputStream. dev-path)
;;         buffer (byte-array 1024)]

;;     (async/thread
;;       (loop []
;;         (let [n (.read in buffer)]
;;           (when (pos? n)
;;             (doseq [i (range n)]
;;               (async/>!! out (bit-and 0xFF (aget buffer i)))))
;;           (recur))))

;;     out))

(defn start-midi-stream [device]
  (let [out (async/chan 100)
        proc (.exec (Runtime/getRuntime)
                    (into-array ["amidi" "-p" device "-d"]))
        in (.getInputStream proc)]

    ;; (async/thread
    ;;   (let [buf (byte-array 1)]
    ;;     (loop []
    ;;       (when (pos? (.read in buf))
    ;;         (async/>!! out (bit-and 0xFF (aget buf 0)))
    ;;         (recur)))))

    (async/thread
  (let [buf (byte-array 1)]
    (loop []
      (let [n (.read in buf)]
        (when (pos? n)
          (async/>!! out (bit-and 0xFF (aget buf 0))))
        (recur)))))
    out))
