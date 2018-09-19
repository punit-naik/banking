(ns banking.db-test
  (:require [banking.resources.db.core :as db]
            [clojure.java.jdbc :as j]
            [clojure.test :refer [deftest testing is]]))

(def ^:pivate sample-data
  '({:account_number 1 :name "punit naik" :balance 0.0}))

(deftest fetch-accounts-test
  (testing "`accounts` table select query tests"
    (with-redefs [j/query (fn [_ _] sample-data)]
      (is (= (db/fetch-accounts {:account_number 1}) (first sample-data))))))

(deftest insert-accounts-test
  (testing "`accounts` table insert query tests"
    (with-redefs [j/insert! (fn [_ _ _] '({:last_insert_rowid 1}))
                  j/query (fn [_ _] sample-data)]
      (is (= (db/insert-account (dissoc (first sample-data) :account_number))
             (first sample-data))))))
