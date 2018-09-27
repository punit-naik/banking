(ns banking.api-test
  (:require [clojure.test :refer [deftest testing is]]
            [banking.core :refer [handler]]
            [banking.resources.db.core :refer [init-db!]]
            [ring.mock.request :as mock]
            [cheshire.core :refer [generate-string parse-string]]
            [clojure.java.shell :refer [sh]]))

(deftest api-test
  (testing "API (GET, POST, PUT, DELETE) tests"
    (let [_ (init-db!)
          _ (handler (mock/request :post "/register-user" {:user-name "punit-naik" :password "test123"}))
          token (get-in (handler (mock/request :post "/login" {:user-name "punit-naik" :password "test123"}))
                        [:headers "authorization"])
          response (handler (-> (mock/request :post "/account" {:name "punit naik"})
                                (mock/header "authorization" token)))
          id (:account_number (try (parse-string (:body response) true) (catch Exception e nil)))
          _ (handler (-> (mock/request :post "/account" {:name "pingal naik"})
                         (mock/header "authorization" token)))]
      (is (= response
             {:status  201
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string {:account_number id :name "punit naik" :balance 0.0})}))
      (is (= (handler (-> (mock/request :get "/account/1")
                          (mock/header "authorization" token)))
             {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string {:account_number id :name "punit naik" :balance 0.0})}))
      (is (= (handler (-> (mock/request :post (str "/account/" id "/deposit") {:amount "100"})
                          (mock/header "authorization" token)))
             {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string {:account_number id :name "punit naik" :balance 100.0})}))
      (is (= (handler (-> (mock/request :post (str "/account/" id "/deposit") {:amount "100"})
                          (mock/header "authorization" token)))
             {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string {:account_number id :name "punit naik" :balance 200.0})}))
      (is (= (handler (-> (mock/request :post (str "/account/" id "/withdraw") {:amount "100"})
                          (mock/header "authorization" token)))
             {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string {:account_number id :name "punit naik" :balance 100.0})}))
      (is (= (handler (-> (mock/request :post (str "/account/" id "/send") {:amount "50" :account_number 2})
                          (mock/header "authorization" token)))
             {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string {:account_number id :name "punit naik" :balance 50.0})}))
      (is (= (handler (-> (mock/request :get (str "/account/" id "/audit"))
                          (mock/header "authorization" token)))
             {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string
                      '({:sequence 0, :description "deposit", :credit 100.0}
                        {:sequence 1, :description "deposit", :credit 100.0}
                        {:sequence 2, :description "withdraw", :debit 100.0}
                        {:sequence 3, :description "sent to #2", :debit 50.0}))}))
      (is (= (handler (-> (mock/request :post (str "/account/" id "/withdraw") {:amount "100"})
                          (mock/header "authorization" token)))
             {:status 403
              :headers {}
              :body "API Error -> Not enough balance!"})))))

(deftest auth-api-test
  (testing "APIs with auth validation"
    (let [_ (init-db!)]
      (is (= (handler (mock/request :post "/account" {:name "punit naik"}))
             {:status 401
              :headers {}
              :body "Not authorized!"})))))

(deftest data-validation-api-test
  (testing "APIs with data validation"
    (let [_ (init-db!)
          _ (handler (mock/request :post "/register-user" {:user-name "pingal-naik" :password "test123"}))
          token (get-in (handler (mock/request :post "/login" {:user-name "pingal-naik" :password "test123"}))
                        [:headers "authorization"])]
      (is (= (handler (-> (mock/request :post "/account" {:name "pingal naik" :amount "test amount"})
                          (mock/header "authorization" token)))
             {:status 400
              :headers {}
              :body "In: [:params :amount] val: \"test amount\" fails spec: :banking.utils/amount at: [:params :amount] predicate: string-is-positive-amount?\n"}))
      (is (= (handler (-> (mock/request :post "/account" {:name "pingal naik" :amount "-100"})
                          (mock/header "authorization" token)))
             {:status 400
              :headers {}
              :body "In: [:params :amount] val: \"-100\" fails spec: :banking.utils/amount at: [:params :amount] predicate: string-is-positive-amount?\n"})))))

(deftest login-api-test
  (testing "APIs with login validation"
    (let [_ (init-db!)
          _ (handler (mock/request :post "/register-user" {:user-name "punit-naik" :password "test123"}))]
      (is (= (handler (mock/request :post "/login" {:user-name "punit-naik" :password "test1234"}))
             {:status 401
              :headers {}
              :body "credentials provided are either wrong or not provided at all!"})))))
