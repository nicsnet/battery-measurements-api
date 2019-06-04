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

(def account-fields [:serial
                     :spree_version
                     :timezone
                     :online
                     :production
                     :charge
                     :discharge
                     :consumption
                     :state_of_charge])

(deftest test-accounts
  (let [timestamp (java.time.LocalDateTime/now)
        update-params {:spree_version 5
                       :serial 123
                       :specified_capacity 2
                       :specified_pv_capacity 4
                       :inverter_power 3
                       :timezone "UTC"
                       :online true
                       :last_seen_at timestamp
                       :measured_at timestamp
                       :production 2
                       :charge 6
                       :discharge 7
                       :consumption 1
                       :state_of_charge 2}]
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
             (-> (db/get-account t-conn {:serial 123})
                 (select-keys [:id :serial :spree_version]))))
      (is (= 1 (db/update-account! t-conn update-params)))
      (is (= {:spree_version 5
              :serial 123
              :timezone "UTC"
              :online true
              :production 2
              :charge 6
              :discharge 7
              :consumption 1
              :state_of_charge 2}
             (-> (db/get-account t-conn {:serial 123})
                 (select-keys account-fields)))))))

(deftest test-measurements
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (is (= 2 (db/create-measurements!
              t-conn
              {:measurements [[111 (java.time.LocalDateTime/now) 1 2 3 4 5]
                              [222 (java.time.LocalDateTime/now) 11 12 13 14 15]]})))))
