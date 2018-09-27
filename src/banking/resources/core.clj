(ns banking.resources.core
  (:require [banking.resources.db.core :as db]
            [banking.utils :refer [with-exception-api]]
            [ring.util.response :refer [response redirect]]
            [buddy.sign.jwt :refer [sign unsign]]
            [cheshire.core :refer [generate-string parse-string]]
            [clojure.string :refer [split join]]))

(defonce ^:private secret (or (System/getenv "APP_SECRET") (System/getProperty "APP_SECRET")))

(defn create-account
  "Creates an account with a `name`"
  [{:keys [params]}]
  (with-exception-api
    (db/insert-account (select-keys params [:name]))
    201))

(defn get-account
  "Gets an account's information (account specified with an ID)"
  [{:keys [route-params]}]
  (with-exception-api
    (db/fetch-accounts {:account_number (:id route-params)})
    200))

(defn deposit-money
  "Deposits some amount in an account"
  [{:keys [params route-params]}]
  (with-exception-api
    (let [account-info (db/fetch-accounts {:account_number (:id route-params)})
          response (db/update-account {:account_number (:id route-params)
                                       :balance (+ (:balance account-info)
                                                   (Float/valueOf (:amount params)))})]
      (db/add-transaction-details {:description (:deposit-type params "deposit")
                                   :amount (Float/valueOf (:amount params)) :type "credit"
                                   :account_number (:id route-params)})
      response)
    200))

(defn withdraw-money
  "Withdraws some amount from an account"
  [{:keys [params route-params]}]
  (with-exception-api
    (let [account-info (db/fetch-accounts {:account_number (:id route-params)})
          response (if (< (:balance account-info) (Float/valueOf (:amount params)))
                     (throw (Exception. "Not enough balance!"))
                     (db/update-account
                       {:account_number (:id route-params)
                        :balance (- (:balance account-info) (Double/valueOf (:amount params)))}))]
      (db/add-transaction-details {:description (:withdraw-type params "withdraw")
                                   :amount (Float/valueOf (:amount params)) :type "debit"
                                   :account_number (:id route-params)})
      response)
    200))

(defn send-money
  "Sends money from one account to another"
  [{:keys [params route-params]}]
  (with-exception-api
    (db/transaction-between-accounts
      {:from_account_number (:id route-params)
       :to_account_number (:account_number params)
       :amount (:amount params)}) 200))

(defn get-audit-logs
  "Gets an account's audit logs (account specified with an ID)"
  [{:keys [route-params]}]
  (with-exception-api
    (db/fetch-transaction-history {:account_number (:id route-params)})
    200))

(defn login
  "Attaches a token in the authorization header upon successful login,
   else throws a 401 exception"
  [{:keys [params headers]}]
  (if-let [user (db/lookup-user (select-keys params [:user-name :password]))]
    {:status 200
     :body "Authenticated!"
     :headers (assoc {} "authorization" (str "Token " (sign user secret)))}
    {:status 401
     :body "credentials provided are either wrong or not provided at all!"}))

(defn logout
  "Logs out by removing he authorization header from the request altogether"
  [request]
  (assoc (redirect "/")
    :headers (dissoc (:headers request) "authorization")))

(defn sign-up
  "Registers a user in the `auth` table"
  [{:keys [params]}]
  (try
    {:status 201
     :body {:user-created (not (empty? (db/register-user (select-keys params [:user-name :password]))))}}
    (catch Exception e
      {:status 401
       :body {:user-created false}})))
