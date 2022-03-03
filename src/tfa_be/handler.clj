(ns tfa-be.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [tfa-be.impl :as api-impl]
            [tfa-be.config :refer [env]]))

(mount.core/start) ;; put here as this namespace is the entrypoint of the app

(s/defschema Result
  {:type (s/enum :success :error :exception)
   :message s/Str
   (s/optional-key :data) {s/Any s/Any}})

;(s/defschema Email (s/ #"^..$"))

(def app
  (api
    {:swagger
     {:ui "/swagger"
      :spec "/swagger.json"
      :data {:info {:title "tfa-be"
                    :description "Two Factor Auth example backend"}
             :tags [{:name "api", :description "APIs"}]}}}

    (context "/api" []
      :tags ["api"]

      (POST "/register" []
        :return Result
        :form-params [email :- (describe s/Regex "Email address")
                      password :- (describe s/Str "Password")]
        :summary "Registers new user credentials"
        (api-impl/register email password))

      (POST "/verify-email" []
        :return Result
        :form-params [email :- (describe s/Str "Email address")
                      token :- (describe s/Str "Token received in the email")]
        :summary "Verifies the registered email"
        (api-impl/verify-email email token))

      (POST "/enable-2fa" []
        :return Result
        :form-params [email :- (describe s/Str "Email address. (Must be already verified)")]
        :summary "Enables two-factor auth for the user."
        (api-impl/enable-2fa email))

      (GET "/config" []
        :return Result
        :summary "Returns list of configurations (env, conf, system.properties etc)"
        (api-impl/get-config)))))


