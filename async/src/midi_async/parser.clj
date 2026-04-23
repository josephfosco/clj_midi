(ns midi-async.parser)

(defn status-byte? [b]
  (>= b 0x80))

(defn channel-message-length [status]
  (let [t (bit-and status 0xF0)]
    (cond
      (or (= t 0xC0) (= t 0xD0)) 1   ;; program change, channel pressure
      (= t 0xB0) 2                   ;; CC
      (= t 0x90) 2                   ;; note on
      (= t 0x80) 2                   ;; note off
      :else 0)))

(defn make-parser []
  (let [state (atom {:status nil
                     :buffer []})]

    (fn [byte]
      (let [{:keys [status buffer]} @state]

        (cond
          ;; 1. NEW STATUS BYTE
          (status-byte? byte)
          (do
            (reset! state {:status byte :buffer []})
            nil)

          ;; 2. DATA BYTE (may use running status)
          :else
          (let [status (or status 0)
                buf (conj buffer byte)
                needed (channel-message-length status)]

            ;; ignore if we don't yet have a valid status
            (if (zero? needed)
              (do
                (reset! state {:status nil :buffer []})
                nil)

              (if (= (count buf) needed)
                (do
                  (reset! state {:status status :buffer []})
                  {:status status
                   :data buf})

                (do
                  (swap! state assoc :buffer buf)
                  nil)))))))))

(defn decode [{:keys [status data]}]
  (let [type (bit-and status 0xF0)
        ch   (bit-and status 0x0F)]

    (cond
      (= type 0x90)
      {:type :note-on
       :channel ch
       :note (first data)
       :velocity (second data)}

      (= type 0x80)
      {:type :note-off
       :channel ch
       :note (first data)
       :velocity 0}

      (= type 0xB0)
      {:type :cc
       :channel ch
       :controller (first data)
       :value (second data)}

      :else
      {:type :unknown
       :status status
       :data data})))
