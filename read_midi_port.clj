;    Copyright (C) 2017-2018, 2023  Joseph Fosco. All Rights Reserved
;
;    This program is free software: you can redistribute it and/or modify
;    it under the terms of the GNU General Public License as published by
;    the Free Software Foundation, either version 3 of the License, or
;    (at your option) any later version.
;
;    This program is distributed in the hope that it will be useful,
;    but WITHOUT ANY WARRANTY; without even the implied warranty of
;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;    GNU General Public License for more details.
;
;    You should have received a copy of the GNU General Public License
;    along with this program.  If not, see <http://www.gnu.org/licenses/>.
(ns read-midi-port
  (:require
   )
  )

(def focusrite-port "20:0") ; replace with your ALSA client:port

(defn listen-midi-alsa []
  (let [proc (.. (ProcessBuilder. ["aseqdump" "-p" focusrite-port])
                 (redirectErrorStream true)
                 start)
        reader (java.io.BufferedReader. (java.io.InputStreamReader. (.getInputStream proc)))]
    (future
      (loop []
        (when-let [line (.readLine reader)]
          (println line)
          (recur))))))

;; (defn listen-midi-alsa2 []
;;   (let [proc (.. (ProcessBuilder. ["aseqdump" "-p" focusrite-port])
;;                  (redirectErrorStream true)
;;                  start)
;;         reader (java.io.BufferedReader. (java.io.InputStreamReader. (.getInputStream proc)))]
;;     (future
;;       (loop []
;;         (when-let [line (.readLine reader)]
;;           (when-let [event (parse-aseqdump line)]
;;             (println event)) ; or process it
;;           (recur))))))

;; (defn parse-aseqdump [line]
;;   ;; example line:
;;   ;; "20:0    Note on                 0   60 127"
;;   (when-let [[_ cmd ch d1 d2] (re-matches
;;                                 #"\s*\S+\s+(\S+(?:\s+\S+)*)\s+(\d+)\s+(\d+)\s+(\d+)"
;;                                 line)]
;;     {:command (keyword (clojure.string/replace cmd #" " "_")) ; :Note_on, :Control_change
;;      :channel (Integer/parseInt ch)
;;      :data1   (Integer/parseInt d1)
;;      :data2   (Integer/parseInt d2)}))


(defn now []
  (/ (System/nanoTime) 1e9)) ; seconds with high precision

(defn listen-midi-alsa []
  (let [proc (.. (ProcessBuilder. ["aseqdump" "-p" focusrite-port])
                 (redirectErrorStream true)
                 start)
        reader (java.io.BufferedReader. (java.io.InputStreamReader. (.getInputStream proc)))]
    (future
      (loop []
        (when-let [line (.readLine reader)]
          (when-let [event (parse-aseqdump line)]
            (println (assoc event :time (now))))
          (recur))))))

(require '[clojure.core.async :as async])

(def midi-chan (async/chan 100))

(defn listen-midi-alsa [port]
  (let [proc (.. (ProcessBuilder. ["aseqdump" "-p" port])
                 (redirectErrorStream true)
                 start)
        reader (java.io.BufferedReader.
                 (java.io.InputStreamReader. (.getInputStream proc)))]
    (future
      (loop []
        (when-let [line (.readLine reader)]
          (when-let [event (parse-aseqdump line)]
            (async/>!! midi-chan event)) ;; send to channel
          (recur))))))

(async/go-loop []
  (when-let [event (async/<! midi-chan)]
    (println "MIDI:" event)
    (recur)))

(listen-midi-alsa "20:0"
  (fn [event]
    (when (= (:command event) :Note_on)
      (let [freq (midi->freq (:data1 event))
            amp  (/ (:data2 event) 127.0)]
        (schedule (+ (now) 0.01)
                  #(send-note freq amp))))))
