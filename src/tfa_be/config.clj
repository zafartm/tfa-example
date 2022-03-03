(ns tfa-be.config
  (:require [cprop.core]
            [cprop.source]
            [mount.core]))

;(defstate env :start (load-config :merge [(args)
;                                          (cprop.source/from-system-props)
;                                          (cprop.source/from-env)]))

(mount.core/defstate env :start (cprop.core/load-config))

(defn get-prop [prop-path]
  (get-in env prop-path))
