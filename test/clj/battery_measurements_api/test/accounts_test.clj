(ns battery-measurements-api.test.accounts-test
  (:require
   [battery-measurements-api.accounts :as a]
   [battery-measurements-api.db.core :as db]
   [clojure.test :refer [is deftest testing]]))

(deftest test-accounts
  (testing "online/offline battery counts"
    (with-redefs [db/online (fn [_] {:total 1337})
                  db/offline (fn [_] {:total 99})]
      (is (= 1337 (a/online-sprees)))
      (is (= 1337 (a/online-eatons)))
      (is (= 99 (a/offline-eatons)))
      (is (= 99 (a/offline-sprees)))))

  (testing "account timezone"
    (with-redefs [db/get-account-by-serial (fn [_] {:timezone "Europe/Berlin"})]
      (is (= "Europe/Berlin" (a/account-timezone 1234)))))

  (testing "find our create account"
    (with-redefs [db/get-account-by-serial (fn [_] "my account")]
      (is (= "my account"  (a/find-or-create-account! 1234)))))

  (testing "current/oudated battery counts"
    (with-redefs [db/current (fn [_] {:total 100})
                  db/outdated (fn [_] {:total 1})]
      (is (= 100 (a/current)))
      (is (= 1 (a/outdated))))))
