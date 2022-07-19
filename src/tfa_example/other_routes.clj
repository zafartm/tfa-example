(ns tfa-example.other-routes
  (:require [compojure.core :refer [defroutes GET POST]]))

(defn post-stripe-webhook [request]
  (clojure.pprint/pprint request)
  (clojure.pprint/pprint (slurp (:body request)))
  (throw (RuntimeException. "debuggin")))



(defroutes
  routes
  (POST "/_/stripe-webhook" request (post-stripe-webhook request)))

