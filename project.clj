(defproject banking "0.1.0"
  :description "Back-end APIs for banking"
  :url "https://github.com/punit-naik/reminders.git"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 ;; Routing
                 [compojure "1.6.1"]
                 ;; Database
                 [org.clojure/java.jdbc "0.7.8"]
                 [java-jdbc/dsl "0.1.3"]
                 [org.xerial/sqlite-jdbc "3.23.1"]
                 ;; Logging
                 [com.taoensso/timbre "4.10.0"]
                 ;; Ring
                 [ring/ring-json "0.4.0"]
                 [ring/ring-mock "0.3.2"]
                 ;; API Throttling
                 [diehard "0.7.1"]
                 ;; Authentication
                 [buddy/buddy-core "1.5.0"]
                 [buddy/buddy-auth "2.1.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [buddy/buddy-sign "3.0.0"]
                 ;; JSON
                 [cheshire "5.8.0"]]
  :plugins [[lein-ring "0.12.4"]]
  :ring {:handler banking.core/handler})
