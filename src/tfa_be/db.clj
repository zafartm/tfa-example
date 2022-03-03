(ns tfa-be.db
  (:require [tfa-be.config :as conf]
            [clojure.java.jdbc :as jdbc]
            [hikari-cp.core :as hikaricp]))


(def ^:private db-conn (delay {:connection-uri (conf/get-prop [:database-url])}))

(defn query []
  (jdbc/query @db-conn "SELECT * FROM users"))



(defn save-new-user [email password-hash])