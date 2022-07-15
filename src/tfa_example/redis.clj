(ns tfa-example.redis
  (:require [tfa-example.config :as config]
            [taoensso.carmine :as carmine]
            [taoensso.carmine.ring :as carmine-ring]))


(defn connection-opts []
  {:spec {:uri (config/get-prop [:redis-uri])
          :db 1}
   :pool {}})

(def ring-session-store (delay (carmine-ring/carmine-store (connection-opts))))


(defn save-token [data]
  {:pre [(map? data)
         (not (clojure.string/blank? (:email data)))]}
  (let [key (str "vtokens:" (:email data))
        ttl 86400]
    (carmine/wcar (connection-opts) (carmine/setex key ttl data))))


(defn get-token [email]
  (let [key (str "vtokens:" email)]
    (carmine/wcar (connection-opts) (carmine/get key))))


(defn delete-token [email]
  (let [key (str "vtokens:" email)]
    (carmine/wcar (connection-opts) (carmine/del key))))

