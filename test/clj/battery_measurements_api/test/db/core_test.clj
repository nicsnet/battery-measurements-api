(ns battery-measurements-api.test.db.core-test
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

(def cellpack-test-data
  [[123 (java.time.LocalDateTime/now) 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23]
   [999 (java.time.LocalDateTime/now) 11 22 33 44 55 66 77 88 99 100 111 122 133 144 155 166 177 188 199 200 210 222 233]])

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
    (db/create-account! t-conn {:id 1 :serial 222 :spree_version 2})
    (is (= 2 (db/create-measurements!
              t-conn
              {:measurements [[111 (java.time.LocalDateTime/now) 1 2 3 4 5]
                              [222 (java.time.LocalDateTime/now) 11 12 13 14 15]]})))
    (is (= {:discharge 11
            :charge 12
            :consumption 13
            :production 14
            :state_of_charge 15}
           (-> (db/first-measurement t-conn {:serial 222})
               (select-keys [:discharge :charge :consumption :production :state_of_charge]))))))

(deftest test-machine-statuses
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (is (= 2 (db/create-machine-statuses!
              t-conn
              {:machine_status [[111 "my-key" "my-value" 0 (java.time.LocalDateTime/now) (java.time.LocalDateTime/now)]
                                [222 "other-key" "other-value" 2 (java.time.LocalDateTime/now) (java.time.LocalDateTime/now)]]})))))

(deftest test-cellpack-data
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (is (= 2 (db/create-cellpack-data!
              t-conn
              {:cellpack_data cellpack-test-data})))))
