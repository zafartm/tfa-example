(ns tfa-example.api_impl
  (:require [ring.util.http-response :as http]
            [ring.util.response :as resp]
            [clojure.tools.logging]
            [tfa-example.config]
            [clojure.string]
            [buddy.hashers]
            [tfa-example.config :as config]
            [tfa-example.emails :as emails]
            [tfa-example.redis :as redis]
            [tfa-example.db :as db]
            [one-time.core :as otp]
            [one-time.qrgen :as qrgen])
  (:import (java.io ByteArrayOutputStream)))


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
  (do-in-try-catch
    (let [email (clojure.string/lower-case email)
          pass-hash (buddy.hashers/derive password)
          totp-secret-key (otp/generate-secret-key)
          verification-token (otp/get-hotp-token totp-secret-key (System/currentTimeMillis))]
      (redis/save-token {:name name :email email :pass_hash pass-hash :totp_secret totp-secret-key :token verification-token})
      (emails/send-template-email email name "Verify your account" "account-verification.html" {:name name :token verification-token})
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
        (do (db/save-new-user (:name found) email (:pass_hash found) (:totp_secret found))
            (success-response "Registration is completed." {:email email}))))))

(defn- check-password? [user-data password]
  (buddy.hashers/check password (:password_hash user-data)))


(defn- check-auth-code? [user-data auth-code]
  (otp/is-valid-totp-token? auth-code (:totp_secret user-data)))


(defn enable-2fa [email password auth-code]
  (do-in-try-catch
    (if-some [user-info (db/find-by-email email)]
      (cond
        (false? (check-password? user-info password))
        (error-response "Invalid password!" {:email email})

        (false? (check-auth-code? user-info auth-code))
        (error-response "Invalid auth code!" {:email email})

        (true? (:is_tfa_enabled user-info))
        (error-response "Two factor auth is already enabled!" {:email email})

        :else
        (do (db/enable-2fa email)
            (success-response "To factor auth is enabled!" {:email email})))

      (error-response "User not found!" {:email email}))))


(defn generate-qr-response [email password]
  (do-in-try-catch
    (if-some [user-info (db/find-by-email email)]
      (let [totp-secret (:totp_secret user-info)
            ^ByteArrayOutputStream qr-stream (qrgen/totp-stream {:label     "Testing 2FA"
                                                                 :user       email
                                                                 :secret     totp-secret
                                                                 :image-size 400
                                                                 :image-type :PNG})
            _ (.flush qr-stream)
            byte-array (.toByteArray qr-stream)
            base64-encoded (ring.util.codec/base64-encode byte-array)]
        (success-response "Base64 encoded image data."
                          {:img_src (str "data:image/png;base64, " base64-encoded)}))
      (error-response "User not found!" {:email email}))))



(defn verify-login [email password auth-code]
  (do-in-try-catch
    (let [user-info (db/find-by-email email)]
      (cond
        (nil? user-info)
        (error-response "User not found!" {:email email})

        (nil? password)
        (error-response "Password is required!")

        (false? (check-password? user-info password))
        (error-response "Invalid password!")

        (and (true? (:is_tfa_enabled user-info))
             (nil? auth-code))
        (error-response "Auth code is required!")

        (and (true? (:is_tfa_enabled user-info))
             (false? (check-auth-code? user-info auth-code)))
        (error-response "Invalid auth code!")

        :else
        (success-response "Login is successful" (select-keys user-info [:full_name :email]))))))



(defn get-config []
  (do-in-try-catch
    (success-response "Configurations" (config/list-all))))


