(ns battery-measurements-api.test.db.core
  (:require
    [battery-measurements-api.db.core :refer [*db*] :as db]
    [luminus-migrations.core :as migrations]
    [clojure.test :refer :all]
    [clojure.java.jdbc :as jdbc]
    [battery-measurements-api.config :refer [env]]
    [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'battery-measurements-api.config/env
      #'battery-measurements-api.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-accounts
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (is (= 1 (db/create-account!
              t-conn
              {:id 1
               :serial 123
               :spree_version 2})))
    (is (= {:id         1
            :serial 123
            :spree_version 2}
           (db/get-account t-conn {:serial 123})))))
