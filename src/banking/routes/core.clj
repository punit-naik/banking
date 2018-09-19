(ns banking.routes.core
  (:require [compojure.core :refer [GET POST PUT DELETE defroutes]]
            [compojure.route :refer [not-found]]
            [banking.resources.core :refer [create-account get-account get-audit-logs
                                              deposit-money withdraw-money send-money
                                              login logout sign-up]]
            [banking.utils :refer [with-auth with-throttler]]))

(defroutes app-routes
  (GET "/" request (with-throttler (fn [_] "Hello World!")))
  (POST "/account" request (with-throttler (with-auth request create-account)))
  (GET "/account/:id{[0-9]+}" request (with-throttler (with-auth request get-account)))
  (POST "/account/:id{[0-9]+}/deposit" request (with-throttler (with-auth request deposit-money)))
  (POST "/account/:id{[0-9]+}/withdraw" request (with-throttler (with-auth request withdraw-money)))
  (POST "/account/:id{[0-9]+}/send" request (with-throttler (with-auth request send-money)))
  (GET "/account/:id{[0-9]+}/audit" request (with-throttler (with-auth request get-audit-logs)))
  (POST "/register-user" request (with-throttler (sign-up request)))
  (POST "/login" request (with-throttler (login request)))
  (POST "/logout" request (with-throttler (logout request)))
  (not-found "<h1>This is not the page you are looking for</h1>
              <p>Sorry, the page you requested was not found!</p>"))
