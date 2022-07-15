(ns tfa-example.utils
  (:require [buddy.core.codecs]
            [buddy.core.bytes])
  (:import (java.nio ByteBuffer)
           (java.util UUID)))

(defn random-uuid []
  (let [buffer (ByteBuffer/allocate (* 2 Long/BYTES))
        uuid (UUID/randomUUID)
        _ (.putLong buffer (.getMostSignificantBits uuid))
        _ (.putLong buffer (.getLeastSignificantBits uuid))
        hex (String. (buddy.core.codecs/bytes->hex (.array buffer)))
        b64u (String. (buddy.core.codecs/bytes->b64u (.array buffer)))]
    {:uuid (.toString uuid)
     :hex  hex
     :b64u b64u}))

;; This is slower as compared to the other version
;; (dotimes [_ 5] (time (dotimes [_ 10000] (tfa-example.utils/random-uuid2))))
;; "Elapsed time: 130.068855 msecs"
;; "Elapsed time: 140.803594 msecs"
;; "Elapsed time: 150.77859 msecs"
;; "Elapsed time: 137.424746 msecs"
;; "Elapsed time: 131.355673 msecs"
(defn- random-uuid2 []
  (let [uuid (UUID/randomUUID)
        raw-bytes (buddy.core.bytes/concat
                    (buddy.core.codecs/long->bytes (.getMostSignificantBits uuid))
                    (buddy.core.codecs/long->bytes (.getLeastSignificantBits uuid)))
        hex (String. (buddy.core.codecs/bytes->hex raw-bytes))
        b64u (String. (buddy.core.codecs/bytes->b64u raw-bytes))]
    {:uuid (.toString uuid)
     :hex  hex
     :b64u b64u}))
