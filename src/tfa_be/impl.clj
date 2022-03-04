(ns tfa-be.impl
  (:require [ring.util.http-response :as http]
            [clojure.tools.logging]
            [tfa-be.config]
            [clojure.string]
            [buddy.hashers]
            [tfa-be.config :as config]
            [tfa-be.emails :as emails]
            [tfa-be.redis :as redis]
            [tfa-be.db :as db]))


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


(defn register [name email password]
  {:pre [(not (clojure.string/blank? name))
         (not (clojure.string/blank? email))
         (not (clojure.string/blank? password))]}

  (do-in-try-catch
    (let [email (clojure.string/lower-case email)
          pass-hash (buddy.hashers/derive password)
          verification-token "??????"] ;; TODO
      (redis/save-token {:name name :email email :pass_hash pass-hash :token verification-token})
      (emails/send-template-email email name "Verify your account" "account-verification.html" {:name name :token verification-token}) ;; TODO
      (success-response "An account verification email is sent." {:email email}))))

(defn verify-email [email token]
  (do-in-try-catch
    (let [email (clojure.string/lower-case email)
          found (redis/get-token email)]
      (cond
        (nil? found)
        (error-response "Verification is failed!" {:email email :token token})

        (not= token (:token found))
        (error-response "Verification is failed!" {:email email :token token})

        :else
        (do (db/save-new-user name email (:pass_hash found))
            (success-response "Registration is completed." {:email email}))))))


(defn enable-2fa [email]
  (do-in-try-catch
    (success-response "Not coded yet")))


(defn get-config []
  (do-in-try-catch
    (success-response "Configurations" (config/list-all))))


