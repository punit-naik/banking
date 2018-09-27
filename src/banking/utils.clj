(ns banking.utils
  (:require [buddy.sign.jwt :refer [sign unsign]]
            [clojure.string :refer [split]]
            [cheshire.core :refer [parse-string]]
            [diehard.core :refer [defratelimiter with-rate-limiter defbulkhead with-bulkhead]]
            [clojure.spec.alpha :as s]))

(defonce ^:private secret (or (System/getenv "APP_SECRET") (System/getProperty "APP_SECRET")))

(defn with-http-error-response
  "Throws an error and catches it, returning the http response passed as an argument"
  [response]
  (try
    (throw (Exception. ""))
    (catch Exception e response)))

(defmacro with-exception-api
  "Wraps the code in an exception when there is an error in executing the API code"
  [api-fn success-status-code]
  `(try
     {:status ~success-status-code
      :body ~api-fn}
     (catch Exception ~'e
       (let [~'ex-info (parse-string (.getMessage ~'e) true)]
         {:status (:err-code ~'ex-info)
          :body (str "API Error -> " (:msg ~'ex-info))}))))

(defn authenticated?
  "Checks if a request is authenticated by checking the JWT in the authorization header"
  [request]
  (try (-> request
           (get-in [:headers "authorization"])
           (split #"\s")
           second
           (unsign secret))
    (catch Exception e nil)))

;; Request map spec
(defonce ^:private string-is-id? (fn [x] (re-matches #"\d+" x)))
(defonce ^:private string-is-positive-amount? (fn [x] (re-matches #"([0-9]*[.])?[0-9]+" x)))
(s/def ::id string-is-id?)
(s/def ::route-params (s/keys :opt-un [::id]))
(s/def ::name string?)
(s/def ::amount string-is-positive-amount?)
(s/def ::account_number string-is-id?)
(s/def ::params (s/keys :opt-un [::name ::amount ::account_number]))
(s/def ::request (s/keys :opt-un [::params ::route-params]))

;; API Throttling

; 100 Requests per second
(defratelimiter my-rl {:rate 100})
; 10 concurrent threads
(defbulkhead my-bh {:concurrency 10})

(defmacro with-throttler
  "Throttles functions based on per second requests and concurrent requests"
  [api-fn]
  `(with-rate-limiter my-rl
     (with-bulkhead my-bh ~api-fn)))

;; Auth, Data Validation, etc.

(defn with-auth
  "Wraps the code in an exception when there is an authentication error while executing the API code"
  [request]
  (if (authenticated? request)
    request
    {:status 401 :body "Not authorized!"}))

(defn with-data-validation
  "Wraps the code in an exception when there is a data validation error while executing the API code"
  [request api-fn]
  (if (:status request)
    (with-http-error-response request)
    (if (s/valid? ::request request)
      (api-fn request)
      (with-http-error-response
        {:status 400
         :body (s/explain-str ::request request)}))))

(defn with-necessary
  "All in one function which wraps auth, data validation and throttling"
  [request api-fn]
  (-> request
      with-auth
      (with-data-validation api-fn)
      with-throttler))
