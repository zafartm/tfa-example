(ns tfa-example.emails
  (:require [tfa-example.config :as conf]
            [clojure.java.io :as io]
            [selmer.parser :as parser]
            [tfa-example.config :as conf])
  (:import (java.util Properties)
           (javax.mail Session Transport Address Authenticator PasswordAuthentication)
           (javax.mail.internet MimeMessage MimeMessage$RecipientType InternetAddress MimeMultipart MimeBodyPart)))

(defn- env-smtp-filename [] (or (conf/get-prop [:emails :smtp_properties]) "smtp.properties"))
(defn- env-from-name [] (conf/get-prop [:emails :from_name]))
(defn- env-from-email [] (conf/get-prop [:emails :from_email]))

(defn- load-smtp-properties []
  (with-open [in (io/input-stream (env-smtp-filename))]
    (let [props (Properties.)]
      (.load props in)
      props)))

(defn- mail-authenticator
  "Creates a proxy for javax.mail.Authenticator to be used for authentication
  during emails. This uses mail.smtp.user (or mail.user) property from smtp
  properties as user name and mail.smtp.password (or mail.password) as password."

  [smtp-props]
  (proxy [Authenticator] []
    (getPasswordAuthentication []
      (let [smtp-user (or (.getProperty smtp-props "mail.smtp.user")
                          (.getProperty smtp-props "mail.user"))
            smtp-password (or (.getProperty smtp-props "mail.smtp.password")
                              (.getProperty smtp-props "mail.password"))]
        (new PasswordAuthentication smtp-user smtp-password)))))

;session is stateful so that it is created only once during the application runtime.
(def ^:private mail-session (delay (let [smtp-props (load-smtp-properties)] (Session/getInstance smtp-props (mail-authenticator smtp-props)))))


(defn- to-address [email, name]
  (new InternetAddress email name))


(defn send-email
  "Sends an email to the recipient using SMTP settings from env."
  [recipient-email, recipient-name, subject, content, content-type]
  (let [session @mail-session
        message (new MimeMessage session)]
    (.setRecipient message MimeMessage$RecipientType/TO (to-address recipient-email recipient-name))
    (.setFrom message (to-address (env-from-email) (env-from-name)))
    (.setSubject message subject)
    (.setContent message content content-type)
    (Transport/send message)))


(defn send-template-email
  "Constructs email content from provided html template then sends it out."
  [recipient-email, recipient-name, subject, template-name, template-data]
  (let [content (parser/render-file
                  (str "email-templates/" template-name)
                  (merge {:recipient-name recipient-name} template-data))
        content-type "text/html;charset=utf-8"]
    (send-email recipient-email, recipient-name, subject, content, content-type)))

