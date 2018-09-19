(ns banking.core
  (:require [banking.routes.core :refer [app-routes]]
            [banking.resources.db.core :refer [init-db!]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-response]]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.sign.jwt :refer [sign unsign]]))

(defonce ^:private secret (or (System/getenv "APP_SECRET") (System/getProperty "APP_SECRET")))
(defonce ^:private backend (jws-backend {:secret secret :options {:alg :hs512}}))

(defn- wrap-handler-with-auth-backend
  "Wraps handler with `wrap-authentication` method of buddy"
  [handler]
  (wrap-authentication handler backend))

(def handler
  (do
    (init-db!)
    (-> app-routes
        wrap-handler-with-auth-backend
        wrap-json-response
        wrap-keyword-params
        wrap-params)))
