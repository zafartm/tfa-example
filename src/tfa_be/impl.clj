(ns tfa-be.impl
  (:require [ring.util.http-response :as http]))

(defn- success-response [message & [data]]
  {:pre [(or (nil? data) (map? data))]}
  (if (some? data)
    (http/ok {:type    "success"
              :message message
              :data    data})
    (http/ok {:type    "success"
              :message message})))


(defn- error-response [message & [data]]
  {:pre [(or (nil? data) (map? data))]}
  (if (some? data)
    (http/ok {:type    "error"
              :message message
              :data    data})
    (http/ok {:type    "error"
              :message message})))


(defmacro ^:private do-in-try-catch [& body]
  `(try
     (do ~@body)
     (catch Throwable ex#
       (clojure.tools.logging/warn ex#)
       (http/ok {:type    "exception"
                 :message (.getMessage ex#)}))))


(defn register [email password]
  (do-in-try-catch
    (success-response "Not coded yet")))

(defn verify-email [email token]
  (do-in-try-catch
    (success-response "Not coded yet")))

(defn enable-2fa [email]
  (do-in-try-catch
    (success-response "Not coded yet")))


