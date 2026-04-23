(ns midi-async.decoder)

(defn status-type [status]
  (bit-and status 0xF0))

(defn channel [status]
  (bit-and status 0x0F))

(defn decode [[s d1 d2]]
  (let [t (status-type s)
        ch (channel s)]
    (cond
      (= t 0x90)
      (if (zero? d2)
        {:type :note-off :note d1 :channel ch}
        {:type :note-on  :note d1 :velocity d2 :channel ch})

      (= t 0x80)
      {:type :note-off :note d1 :velocity d2 :channel ch}

      (= t 0xB0)
      {:type :cc :cc d1 :value d2 :channel ch}

      :else
      {:type :unknown :raw [s d1 d2]})))
