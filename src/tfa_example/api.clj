(ns tfa-example.api
  (:require [compojure.api.sweet :refer :all]
            [compojure.api.meta :refer [restructure-param]]
            [ring.util.http-response :refer :all]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.accessrules :refer [restrict]]
            [schema.core :as s]
            [tfa-example.api-impl :as api-impl]
            [tfa-example.stripe-api-impl :as stripe-api-impl]
            [tfa-example.stripe-events :as stripe-events]))


(defmethod restructure-param :current-request
  [_ binding acc]
  (update-in acc [:letks] into [binding `~'+compojure-api-request+]))


(defmethod restructure-param :current-session
  [_ binding acc]
  (update-in acc [:letks] into [binding `(-> ~'+compojure-api-request+ :session)]))


(defmethod restructure-param :current-user
  [_ binding acc]
  (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

(defmethod restructure-param :current-uid
  [_ binding acc]
  (update-in acc [:letks] into [binding `(some-> ~'+compojure-api-request+ :identity :id)]))


(defn access-error [_ _]
  (unauthorized {:type "error"
                 :message "Authorization is failed!"}))

(defn wrap-restricted [handler rule]
  (restrict handler {:handler  rule
                     :on-error access-error}))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-restricted rule]))

(defn is-logged-in?
  [req]
  (authenticated? req))


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
      :tags ["Core API"]

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

      (POST "/login" []
        :return Result
        :form-params [email :- (describe s/Str "Email address")
                      password :- (describe s/Str "Password")
                      {auth_code :- (describe s/Int "(Optional) Code from Authenticator") nil}]
        :summary "Verifies login credentials. Then creates a cookie based auth session"
        (api-impl/login-session email password auth_code))

      (POST "/logout" []
        :return Result
        :auth-rules is-logged-in?
        :summary "Removes auth session"
        (api-impl/logout-session))

      (GET "/session" []
        ;:return Result
        :summary "Checks if cookie based auth session is valid."
        :current-user userinfo
        :current-request req
        (api-impl/check-session userinfo))

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
        :auth-rules is-logged-in?
        :current-request req
        (clojure.pprint/pprint req)
        (api-impl/get-config))

      (POST "/stripe-webhook" request
        :summary "Webhook end point"
        :coercion nil
        (do
          ;(clojure.pprint/pprint (slurp (:body request)))
          (stripe-events/record-stripe-event (slurp (:body request)))
          (ok {:message "Event receipt is acknowledged."})))

      (context "/stripe" []
        :tags ["Stripe"]
        :auth-rules is-logged-in?

        (GET "/list-prices" []
          :summary "Lists available prices with ids"
          (stripe-api-impl/list-prices))

        (POST "/subscribe-request" []
          :summary "Creates a checkout session"
          :form-params [selected_price :- (describe s/Str "Selected price id")]
          :current-user userinfo
          (stripe-api-impl/create-checkout-session (:id userinfo) selected_price))

        (GET "/subscribe-success" []
          :summary "Return url for checkout session")

        (GET "/subscribe-cancel" []
          :summary "Return url for checkout session")

        (POST "/portal" []
          :summary "Created a billing portal session"
          :current-user userinfo
          (stripe-api-impl/create-portal-session (:id userinfo)))

        (GET "/portal-return" []
          :summary "Return url from billing portal")))))






