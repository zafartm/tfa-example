(ns tfa-be.db
  (:require [tfa-be.config :as conf]
            [clojure.java.jdbc :as jdbc]))


(def mysql-db {:connection-uri (conf/get-prop [:database-url])})


(defn query []
  (jdbc/query mysql-db "SELECT * FROM users"))



(defn save-new-user [email password-hash])