(ns tfa-example.handler
  (:require [compojure.api.sweet]
            [compojure.route]
            [compojure.core]
            [ring.util.http-response]
            [tfa-example.api]))

(def app
  (compojure.api.sweet/routes
    #'tfa-example.api/api-routes
    (compojure.route/resources "/")
    (compojure.core/GET "/" _ (ring.util.http-response/temporary-redirect "index.html"))
    (compojure.route/not-found "Not found")))
