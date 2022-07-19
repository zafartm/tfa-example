(ns tfa-example.stripe-events
  (:require [tfa-example.api-impl-helper :refer :all]
            [tfa-example.db :as db]
            [tfa-example.stripe :as stripe]
            [tfa-example.utils :as utils]
            [clojure.tools.logging :as logging])
  (:import (java.sql SQLIntegrityConstraintViolationException)))


(defn- process-checkout-session-completed [event-data]
  (clojure.pprint/pprint event-data))

(defn- process-payment-succeeded-stripe [event-data]
  (clojure.pprint/pprint event-data))

(defn- process-payment-failed [event-data]
  (clojure.pprint/pprint event-data))

(defn- verify-and-process-event [event-id]
  (try
    (let [event-data (stripe/get-event-data event-id)
          result (case (:type event-data)
                   "checkout.session.completed" (process-checkout-session-completed event-data)
                   "invoice.payment_succeeded" (process-payment-succeeded-stripe event-data)
                   "invoice.payment_failed" (process-payment-failed event-data)
                   ;"customer.source.expiring" (process-source-expiring event-data)
                   ;"customer.subscription.deleted" (process-subscription-deleted event-data)
                   ;"charge.dispute.funds_withdrawn" (process-dispute-funds-withdrawn event-data)
                   ;"charge.dispute.closed" (process-dispute-closed event-data)
                   ;"charge.refunded" (process-charge-refunded event-data)
                   ;; else
                   :ignored)]
      (if (= :ignored result)
        (db/update-stripe-event event-id :ignored nil)
        (db/update-stripe-event event-id :processed nil)))
    (catch Throwable ex
      (db/update-stripe-event event-id :error (utils/ex-cause-trace ex)))))


(defn record-stripe-event [event-data]
  (if-some [event-id (:id (utils/json-to-clojure event-data))]
    (try
      (db/save-stripe-event event-id event-data)
      (verify-and-process-event event-id)
      (catch SQLIntegrityConstraintViolationException ex
        (logging/warn (.getMessage (clojure.stacktrace/root-cause ex)))))
    (logging/warn "Stripe event is received without any id!" event-data)))

