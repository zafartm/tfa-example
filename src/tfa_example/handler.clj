(ns tfa-example.handler
  (:require [compojure.api.sweet]
            [compojure.route]
            [tfa-example.api :as api]))

(def app
  (compojure.api.sweet/routes
    #'api/api-routes
    (compojure.route/resources "/")
    (compojure.route/not-found "Not found")))
