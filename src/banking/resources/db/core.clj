(ns banking.resources.db.core
  (:require [clojure.java.jdbc :as j]
            [java-jdbc.ddl :as ddl]
            [cheshire.core :refer [generate-string]]
            [buddy.hashers :as hashers]))

(defonce ^:private db-conf
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "accounts.db"})

(defn init-db!
  "Create all the necessary tables!"
  []
  (try
    (do
      (j/db-do-commands db-conf
        (ddl/create-table :accounts
          [:account_number "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL"]
          [:name "text not null UNIQUE"]
          [:balance "REAL DEFAULT 0.0 CHECK (balance >= 0.0) NOT NULL"]))
      (j/db-do-commands db-conf
        (ddl/create-table :transaction_history
          [:sequence "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL"]
          [:description "text NOT NULL"]
          [:amount "REAL NOT NULL CHECK (amount >= 0.0)"]
          [:type "varchar(6) NOT NULL"]
          [:account_number "INTEGER NOT NULL"]))
      (j/db-do-commands db-conf
        (ddl/create-table :auth
          [:id "INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL"]
          [:user_name "text UNIQUE NOT NULL"]
          [:password "text UNIQUE NOT NULL"])))
    (catch Exception e (.getMessage e))))

(defn fetch-accounts
  "Gets the accounts from the `accounts` table"
  [{:keys [account_number]}]
  (let [result (j/query db-conf
                 ["select * from accounts where account_number = ?" account_number])]
    (if (empty? (first result))
      (throw (Exception. (generate-string {:msg "Account not present!" :err-code 404})))
      (first result))))

(defn insert-account
  "Inserts an account in the `accounts` table"
  [row]
  (fetch-accounts {:account_number (first (vals (last (j/insert! db-conf :accounts row))))}))

(defn update-account
  "Updates an account with a specific ID from the `accounts` table"
  [{:keys [account_number] :as row}]
  (when (seq (dissoc row :account_number))
    (j/update! db-conf :accounts (dissoc row :account_number) ["account_number = ?" account_number]))
  (fetch-accounts {:account_number account_number}))

(defn add-transaction-details
  "Adds the details about the transaction in the `transaction_history` table"
  ([details] (add-transaction-details details db-conf))
  ([details db-spec] (j/insert! db-spec :transaction_history details)))

(defn fetch-transaction-history
  "Gets the transaction history from the `transaction_history` table"
  [{:keys [account_number]}]
  (let [result (map
                 (fn [row]
                   (assoc (dissoc row :amount :type :account_number) (keyword (:type row)) (:amount row)))
                 (j/query db-conf
                   ["select * from transaction_history where account_number = ?" account_number]))]
    (if (empty? result)
      (throw (Exception. (generate-string {:msg "Audit logs not present!" :err-code 404})))
      (map-indexed #(assoc %2 :sequence %1) (sort-by :sequence result)))))

(defn transaction-between-accounts
  "Transfers money from one account to another using database transactions"
  [{:keys [from_account_number to_account_number amount]}]
  (if (= from_account_number to_account_number)
    (throw (Exception. (generate-string {:msg "Cannot send money from an account to itself!" :err-code 403})))
    (try
      (do
        (j/with-db-transaction [tx-conn db-conf]
          (j/execute! tx-conn ["update accounts set balance = balance - ? where account_number = ? and balance >= 0.0"
                               amount from_account_number])
          (j/execute! tx-conn ["update accounts set balance = balance + ? where account_number = ?"
                               amount to_account_number])
          (add-transaction-details
            {:description (str "sent to #" to_account_number)
             :type "debit" :amount amount
             :account_number from_account_number} tx-conn)
          (add-transaction-details
            {:description (str "received from #" from_account_number)
             :type "credit" :amount amount
             :account_number to_account_number} tx-conn))
        (fetch-accounts {:account_number from_account_number}))
      (catch Exception e
        (throw (Exception. (generate-string {:msg "Not enough balance in the sender's account!" :err-code 403})))))))

(defn delete-reminder
  "Deletes a reminder with a specific ID from the `banking` table"
  [reminder-id]
  (= (first (j/delete! db-conf :banking ["id = ?" reminder-id])) 1))

(defn register-user
  "Registers a user with a `user_name` and a `password`"
  [{:keys [user-name password]}]
  (j/insert! db-conf :auth {:user_name user-name :password (hashers/derive password)}))

(defn lookup-user
  "Searches for a user in the `auth` table by their `user_name` and `password`"
  [{:keys [user-name password]}]
  (when-let [row (first (j/query db-conf ["select user_name, password from auth where user_name = ?" user-name]))]
    (when (hashers/check password (:password row))
      row)))
