(ns tfa-be.db
  (:require [tfa-be.config :as conf]
            [clojure.java.jdbc :as jdbc]
            [hikari-cp.core :as hikaricp]))


(def ^:private db-conn (delay {:connection-uri (conf/get-prop [:database-url])}))

(def ^:private datasource
  (delay (hikaricp/make-datasource {:jdbc-url (conf/get-prop [:database-url])})))

(comment
  "Plain connection without pooling"
  (jdbc/query @db-conn "SELECT * FROM users"))

(comment
  "Pooled connection using hikari-cp pool"
  (jdbc/with-db-connection
    [conn {:datasource @datasource}]
    (jdbc/query conn "SELECT * FROM users")))

(comment
  "Pooled connection and db transaction"
  (jdbc/with-db-transaction
    [conn {:datasource @datasource} {:read-only? true}]
    (jdbc/query conn "SELECT * FROM users")))


(defn save-new-user [name email password-hash totp-secret]
  (jdbc/with-db-connection
    [conn {:datasource @datasource}]
    (jdbc/insert! conn :users {:full_name name :email email :password_hash password-hash :totp_secret totp-secret})))
