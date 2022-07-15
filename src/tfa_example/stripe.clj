(ns tfa-example.stripe
  (:require [tfa-example.config]
            [tfa-example.utils :as utils]
            [clojure.tools.logging])

  (:import (com.stripe Stripe)
           (com.stripe.model StripeObject Price Product)
           (com.stripe.exception StripeException)))

(defn- secret-key []
  (tfa-example.config/get-prop [:stripe :secret-key]))


(defn- init-stripe
  "Initializes Stripe API with secret key from env."
  []
  (set! (Stripe/apiKey) (secret-key))
  (Stripe/setMaxNetworkRetries 3))


(defmacro ^:private to-json
  "Converts a StripeObject to a json string."
  [^StripeObject stripe-obj]
  `(.toJson StripeObject/PRETTY_PRINT_GSON ~stripe-obj))


(defmacro ^:private to-clojure
  "Converts a StripeObject to clojure data structure."
  [^StripeObject stripe-obj]
  `(utils/json-to-clojure (to-json ~stripe-obj)))


(defmacro ^:private with-stripe-ex-logging
  "Evaluates the body in a try catch. If some StripeException occurs, extracts
  the message and rethrow the ex-info object."
  [& body]
  `(try
     ~@body
     (catch StripeException stripe-ex#
       (clojure.tools.logging/warn "Stripe error!" (to-clojure (.getStripeError stripe-ex#)))
       (throw (ex-info (.getUserMessage stripe-ex#) {})))
     (catch Throwable ex#
       (throw ex#))))


(defn list-all-products []
  (with-stripe-ex-logging
    (init-stripe)
    (to-clojure
      (-> (Product/list {"active" true})
          (.getData)))))

(defn list-product-prices [product-id]
  (with-stripe-ex-logging
    (init-stripe)
    (to-clojure
      (-> (Price/list {"product" product-id})
          (.getData)))))

