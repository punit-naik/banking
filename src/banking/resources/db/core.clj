(ns banking.resources.db.core
  (:require [clojure.java.jdbc :as j]
            [java-jdbc.ddl :as ddl]
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
          [:account_number "INTEGER PRIMARY KEY AUTOINCREMENT"]
          [:name "text not null"]
          [:balance "REAL DEFAULT 0.0"]))
      (j/db-do-commands db-conf
        (ddl/create-table :transaction_history
          [:sequence "INTEGER PRIMARY KEY AUTOINCREMENT"]
          [:description "text"]
          [:amount "REAL"]
          [:type "varchar(6)"]
          [:account_number "INTEGER"]))
      (j/db-do-commands db-conf
        (ddl/create-table :auth
          [:id "INTEGER PRIMARY KEY AUTOINCREMENT"]
          [:user_name "text UNIQUE"]
          [:password "text UNIQUE"])))
    (catch Exception e (.getMessage e))))

(defn fetch-accounts
  "Gets the accounts from the `accounts` table"
  [{:keys [account_number]}]
  (let [result (j/query db-conf
                 ["select * from accounts where account_number = ?" account_number])]
    (if (empty? (first result))
      (throw (Exception. "Account not present!"))
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
  [details]
  (j/insert! db-conf :transaction_history details))

(defn fetch-transaction-history
  "Gets the transaction history from the `transaction_history` table"
  [{:keys [account_number]}]
  (let [result (map
                 (fn [row]
                   (assoc (dissoc row :amount :type :account_number) (keyword (:type row)) (:amount row)))
                 (j/query db-conf
                   ["select * from transaction_history where account_number = ?" account_number]))]
    (if (empty? result)
      (throw (Exception. "Audit logs not present!"))
      (map-indexed #(assoc %2 :sequence %1) (sort-by :sequence result)))))

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
