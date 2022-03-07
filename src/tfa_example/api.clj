(ns tfa-example.api
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [tfa-example.api_impl :as api-impl]))

(s/defschema Result
  {:type (s/enum :success :error :exception)
   :message s/Str
   (s/optional-key :data) {s/Any s/Any}})

;(s/defschema Email (s/ #"^..$"))

(def api-routes
  (api
    {:swagger
     {:ui "/swagger"
      :spec "/swagger.json"
      :data {:info {:title "2FA example API"}
             :tags [{:name "api", :description "APIs"}]}}}

    (context "/api" []
      :tags ["api"]

      (POST "/register" []
        :return Result
        :form-params [name :- (describe s/Str "Full name")
                      email :- (describe s/Str "Email address")
                      password :- (describe s/Str "Password")]
        :summary "Registers new user credentials"
        (api-impl/register name email password))

      (POST "/verify-email" []
        :return Result
        :form-params [email :- (describe s/Str "Email address")
                      token :- (describe s/Int "Token received in the email")]
        :summary "Verifies the registered email"
        (api-impl/verify-email email token))

      (POST "/verify-login" []
        :return Result
        :form-params [email :- (describe s/Str "Email address")
                      password :- (describe s/Str "Password")
                      {auth_code :- (describe s/Int "(Optional) Code from Authenticator") nil}]
        :summary "Verifies login credentials."
        (api-impl/verify-login email password auth_code))

      (POST "/enable-2fa" []
        :return Result
        :form-params [email :- (describe s/Str "Email address")
                      password :- (describe s/Str "Password")
                      auth_code :- (describe s/Int "Code from Authenticator")]
        :summary "Enables two-factor auth for the user."
        (api-impl/enable-2fa email password auth_code))

      (GET "/totp-code" []
        :query-params [email :- (describe s/Str "Email address. (Must be already verified)")
                       password :- (describe s/Str "Password")]
        :summary "Returns QR image for Authenticator config."
        (api-impl/generate-qr-response email password))

      (GET "/config" []
        :return Result
        :summary "Returns list of configurations (env, conf, system.properties etc)"
        (api-impl/get-config)))))

