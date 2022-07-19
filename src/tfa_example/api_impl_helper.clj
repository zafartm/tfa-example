(ns tfa-example.api-impl-helper
  (:require [ring.util.http-response :as http]
            [clojure.tools.logging]))


(defn success-response [message & [data]]
  {:pre [(or (nil? data) (map? data))]}
  (if (some? data)
    (http/ok {:type    "success"
              :message message
              :data    data})
    (http/ok {:type    "success"
              :message message})))


(defn error-response [message & [data]]
  {:pre [(or (nil? data) (map? data))]}
  (if (some? data)
    (http/ok {:type    "error"
              :message message
              :data    data})
    (http/ok {:type    "error"
              :message message})))


(defmacro do-in-try-catch [& body]
  `(try
     (do ~@body)
     (catch Throwable ex#
       (clojure.tools.logging/warn ex#)
       (http/ok {:type    "exception"
                 :message (.getMessage ex#)}))))

