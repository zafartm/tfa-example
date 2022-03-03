(ns tfa-be.config
  (:require [cprop.core]
            [cprop.source]))

(def ^:private env (delay (cprop.core/load-config)))


(defn list-all [] @env)

(defn get-prop [prop-path]
  (get-in @env prop-path))
