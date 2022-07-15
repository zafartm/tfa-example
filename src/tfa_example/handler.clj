(ns tfa-example.handler
  (:require [compojure.api.sweet]
            [compojure.route]
            [compojure.core]
            [ring.middleware.session]
            [ring.util.http-response]
            [buddy.auth.backends]
            [buddy.auth.middleware]
            [tfa-example.api]
            [tfa-example.redis :refer [ring-session-store]]))

(def ^:private auth-backend (delay (buddy.auth.backends/session)))

(def app
  (-> (compojure.api.sweet/routes
        #'tfa-example.api/api-routes
        (compojure.route/resources "/")
        (compojure.core/GET "/" _ (ring.util.http-response/temporary-redirect "index.html"))
        (compojure.route/not-found "Not found"))
      (buddy.auth.middleware/wrap-authentication @auth-backend)
      (buddy.auth.middleware/wrap-authorization @auth-backend)
      (ring.middleware.session/wrap-session {:store @ring-session-store})))

