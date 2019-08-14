(ns battery-measurements-api.test.db.accounts-test
  (:require
   [battery-measurements-api.db.core :refer [*db*] :as db]
   [luminus-migrations.core :as migrations]
   [clojure.test :refer :all]
   [clojure.java.jdbc :as jdbc]
   [battery-measurements-api.config :refer [env]]
   [java-time :refer [local-date-time hours minus days minutes]]
   [mount.core :as mount]))

(def timestamps {:created_at (local-date-time) :updated_at (local-date-time)})

(def in-us (rand-int 10000))

(def in-wipo (rand-nth ["178.19.214.131" "185.19.198.179" "213.182.26.2" "24.134.68.117" "58.171.8.1"]))

(def online-eaton
  (let [last-seen (minus (local-date-time) (hours 11))]
    (merge timestamps  {:serial 12345
                        :wan_ip "not_in_wipo"
                        :spree_version 0
                        :hw_version 5
                        :last_seen_at last-seen})))
(def offline-eaton
  (let [last-seen (minus (local-date-time) (days 1))]
    (merge timestamps  {:serial (rand-int 10000)
                        :wan_ip "not_in_wipo"
                        :spree_version 0
                        :hw_version 5
                        :last_seen_at last-seen})))

(def online-spree
  (let [last-seen (local-date-time)]
    (merge timestamps  {:serial (rand-int 10000)
                        :wan_ip "not_in_wipo"
                        :spree_version 1
                        :hw_version 8
                        :last_seen_at last-seen})))
(def offline-spree
  (let [last-seen (minus (local-date-time) (minutes 30))]
    (merge timestamps  {:serial (rand-int 10000)
                        :wan_ip "not_in_wipo"
                        :spree_version 2
                        :hw_version 8.5
                        :last_seen_at last-seen})))

(def online-spree
  (let [last-seen (local-date-time)]
    (merge timestamps  {:serial (rand-int 10000)
                        :wan_ip "not_in_wipo"
                        :spree_version 1
                        :hw_version 8
                        :last_seen_at last-seen})))

(def online-spree-in-wipo
  (let [last-seen (local-date-time)]
    (merge timestamps  {:serial (rand-int 10000)
                        :wan_ip in-wipo
                        :spree_version 2
                        :hw_version 8.5
                        :last_seen_at last-seen})))

(def online-eaton-in-us
  (let [last-seen (local-date-time)]
    (merge timestamps  {:serial in-us
                        :wan_ip "in-us"
                        :spree_version 0
                        :hw_version 4
                        :last_seen_at last-seen})))

(defn create-machine-setting-us []
  (db/create-machine-setting! {:serial in-us
                               :key "update_channel"
                               :value "us-stable"
                               :version (rand-int 10)}))
(defn seed-db [db-connection]
  (create-machine-setting-us)
  (jdbc/insert-multi! db-connection :accounts
                      [online-spree-in-wipo
                       online-eaton-in-us
                       online-spree
                       offline-spree
                       online-eaton
                       offline-eaton]))

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
    (jdbc/with-db-transaction [con *db*]
      (jdbc/db-set-rollback-only! con)
      (is (= 1 (db/create-account!
                con
                {:id 1
                 :serial 123
                 :spree_version 2})))
      (is (= {:id 1
              :serial 123
              :spree_version 2}
             (-> (db/get-account-by-serial con {:serial 123})
                 (select-keys [:id :serial :spree_version]))))
      (is (= 1 (db/update-account! con update-params)))
      (is (= {:spree_version 5
              :serial 123
              :timezone "UTC"
              :online true
              :production 2
              :charge 6
              :discharge 7
              :consumption 1
              :state_of_charge 2}
             (-> (db/get-account-by-serial con {:serial 123})
                 (select-keys account-fields)))))))

(deftest online-offline-db-queries
  (jdbc/with-db-transaction [con *db*]
    (jdbc/db-set-rollback-only! con)
    (seed-db con)
    (is (= {:total 1} (db/offline
                       con
                       {:spree false
                        :exclude-devices (db/us-devices)
                        :exclude-ip-addresses (db/ignored-ips)})))
    (is (= {:total 1} (db/offline
                       con
                       {:spree true
                        :exclude-devices (db/us-devices)
                        :exclude-ip-addresses (db/ignored-ips)})))
    (is (= {:total 1} (db/online
                       con
                       {:spree false
                        :exclude-devices (db/us-devices)
                        :exclude-ip-addresses (db/ignored-ips)})))
    (is (= {:total 1} (db/online
                       con
                       {:spree true
                        :exclude-devices (db/us-devices)
                        :exclude-ip-addresses (db/ignored-ips)})))
    (is (= [{:total 6}] (jdbc/query con ["select count(*) as total from accounts"])))))
