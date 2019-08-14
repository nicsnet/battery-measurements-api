(ns battery-measurements-api.test.accounts-test
  (:require
   [battery-measurements-api.accounts :refer :all]
   [battery-measurements-api.db.core :as db]
   [battery-measurements-api.measurements :refer [first-measurement]]
   [clojure.test :refer :all]
   [java-time :as time]]))

(deftest test-accounts
(testing "online/offline battery counts"
  (with-redefs [db/online (fn [_] {:total 1337})
                db/offline (fn [_] {:total 99})]
    (is (= 1337 (online-sprees)))
    (is (= 1337 (online-eatons)))
    (is (= 99 (offline-eatons)))
    (is (= 99 (offline-sprees)))))

(testing "account timezone"
  (with-redefs [db/get-account-by-serial (fn [_] {:timezone "Europe/Berlin"})]
    (is (= "Europe/Berlin" (account-timezone 1234)))))

(testing "account attributes"
  (let [settings {:spree_version 1
                  :capacity_kw 2.3
                  :inverter_power_kw 1.2
                  :pvsize_kw 2.2}]
    (with-redefs [first-measurement (fn [_] {:discharge 2
                                            :charge 3
                                            :consumption 4
                                            :production 2
                                            :state_of_charge 7
                                            :timestamp "some timestamp"})
                  time/local-date-time (fn [_] "it's time")]
      (is (= {:charge 3
              :consumption 4
              :discharge 2
              :inverter_power 1.2
              :last_seen_at "it's time"
              :online true
              :production 2
              :serial 1234
              :specified_capacity 2.3
              :specified_pv_capacity 2.2
              :spree_version 1
              :state_of_charge 7
              :timestamp "some timestamp"
              :timezone nil}
             (account-attributes settings 1234)))))))
