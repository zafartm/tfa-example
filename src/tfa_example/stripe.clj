(ns tfa-example.stripe
  (:require [tfa-example.config]
            [tfa-example.utils :as utils]
            [clojure.tools.logging])

  (:import (com.stripe Stripe)
           (com.stripe.model StripeObject Price Product Event Customer Subscription SubscriptionSearchResult PaymentMethod)
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


(defn create-checkout-session [current-user-id customer-id customer-email success-url cancel-url price-id trial-days discount-coupon]
  (with-stripe-ex-logging
    (init-stripe)
    (to-clojure
      (let [parameters (cond->
                         {"client_reference_id"  current-user-id
                          "mode"                 "subscription"
                          "line_items"           [{"price" price-id
                                                   "quantity" 1}]
                          "payment_method_types" ["card"]
                          ;"payment_intent_data"  {"setup_future_usage" "off_session"}
                          "success_url"          success-url
                          "cancel_url"           cancel-url}
                         (some? trial-days) (assoc "subscription_data" {"trial_period_days" trial-days})
                                                                        ;"payment_behavior"  "error_if_incomplete"})
                         ;(nil? trial-days) (assoc "subscription_data" {"payment_behavior"  "error_if_incomplete"})
                         (some? discount-coupon) (assoc "discounts" {"coupon" discount-coupon})
                         (some? customer-id) (assoc "customer" customer-id)
                         (nil? customer-id) (assoc "customer_email" customer-email))]
        (com.stripe.model.checkout.Session/create parameters)))))



(defn create-portal-session [customer-id return-url]
  (with-stripe-ex-logging
    (init-stripe)
    (to-clojure
      (com.stripe.model.billingportal.Session/create {"customer" customer-id
                                                      "return_url" return-url}))))

(defn get-customer-details [customer-id]
  (with-stripe-ex-logging
    (init-stripe)
    (to-clojure
      (Customer/retrieve customer-id))))

(defn list-payment-methods [customer-id]
  (with-stripe-ex-logging
    (init-stripe)
    (to-clojure
      (-> (PaymentMethod/list {"customer" customer-id
                               "type" "card"})
          (.getData)))))

(defn cancel-all-subscriptions []
  (with-stripe-ex-logging
    (init-stripe)
    (to-clojure
      (let [^SubscriptionSearchResult search-result (Subscription/search {"query" "status:'active'"
                                                                          "limit" 100})]
        (loop [list-to-cancel (.getData search-result)
               next-page (.getNextPage search-result)]
          (doall (for [s list-to-cancel] (.update s {"cancel_at_period_end" true})))
          (when (some? next-page)
            (let [new-result (Subscription/search {"query" "status:'active'"
                                                   "limit" 100
                                                   "page"  next-page})]
              (recur (.getData new-result)
                     (.getNextPage new-result)))))))))



;
;(defn cancel-all-subscription []
;  (with-stripe-ex-logging
;    (init-stripe)
;    (let [search-result (com.stripe.model.Subscription/search {"query" "status:'active'"})]
;      (doall (for [s all-active] (.update s {"cancel_at_period_end" true})))
;      (count all-active))))
