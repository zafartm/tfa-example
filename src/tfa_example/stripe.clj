(ns tfa-example.stripe
  (:require [tfa-example.config]
            [tfa-example.utils :as utils]
            [clojure.tools.logging])

  (:import (com.stripe Stripe)
           (com.stripe.model StripeObject Price Product Event)
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
       (let [stripe-err# (to-clojure (.getStripeError stripe-ex#))]
         (clojure.tools.logging/warn "Stripe error!" stripe-err#)
         (throw (ex-info (.getUserMessage stripe-ex#) stripe-err#))))
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

(defn get-price-data [price-id]
  (with-stripe-ex-logging
    (init-stripe)
    (to-clojure
      (Price/retrieve price-id))))

(defn get-event-data [event-id]
  (with-stripe-ex-logging
    (init-stripe)
    (to-clojure
      (Event/retrieve event-id))))


(defn create-checkout-session [current-user-id customer-id customer-email success-url cancel-url price-id]
  (with-stripe-ex-logging
    (init-stripe)
    (to-clojure
      (let [parameters {"client_reference_id"  current-user-id
                        "line_items"           [{"price"    price-id "quantity" 1}]
                        "payment_method_types" ["card"]
                        "mode"                 "subscription"
                        ;"subscription_data"    (if (pos? trial-period-days)
                        ;                         {"trial_period_days" trial-period-days}
                        ;                         {"trial_from_plan" false})
                        "success_url"          success-url
                        "cancel_url"           cancel-url}]
        (com.stripe.model.checkout.Session/create
          (if (some? customer-id)
            (assoc parameters "customer" customer-id)
            (assoc parameters "customer_email" customer-email)))))))


(defn create-portal-session [customer-id return-url]
  (with-stripe-ex-logging
    (init-stripe)
    (to-clojure
      (com.stripe.model.billingportal.Session/create {"customer" customer-id
                                                      "return_url" return-url}))))