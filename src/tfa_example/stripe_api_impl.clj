(ns tfa-example.stripe-api-impl
  (:require [tfa-example.api-impl-helper :refer :all]
            [tfa-example.db :as db]
            [tfa-example.stripe :as stripe]))


(defn list-prices []
  (do-in-try-catch
    (success-response
      "List of available product prices."
      {:list (for [p (tfa-example.config/get-prop [:stripe :prices])]
               {:title    (:name p)
                :price    (:price p)
                :price_id (:price-id p)})})))


(defn create-checkout-session [current-user-id price-id]
  (do-in-try-catch
    (let [current-user (db/find-by-id current-user-id)
          customer-id (:stripe_customer_id current-user)
          customer-email (:email current-user)
          success-url "http://localhost:3001/api/stripe/subscribe-success"
          cancel-url "http://localhost:3001/api/stripe/subscribe-cancel"
          session-data (stripe/create-checkout-session current-user-id
                                                       customer-id
                                                       customer-email
                                                       success-url
                                                       cancel-url
                                                       price-id
                                                       1
                                                       nil)]

      (success-response "Checkout session is created" session-data))))


(defn create-portal-session [current-user-id]
  (do-in-try-catch
    (if-some [customer-id (:stripe_customer_id (db/find-by-id current-user-id))]
      (let [return-url "http://localhost:3001/api/stripe/portal-return"
            session-data (stripe/create-portal-session customer-id return-url)]
        (success-response "Portal session is created" session-data))
      (error-response "Portal session cannot be created when customer_id is missing for user."))))

