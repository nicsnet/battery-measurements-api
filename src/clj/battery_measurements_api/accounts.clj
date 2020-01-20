(ns battery-measurements-api.accounts
  (:require [clojure.string :refer [blank?]]
            [conman.core :as conman]
            [java-time :as time]
            [battery-measurements-api.db.core :as db]
            [battery-measurements-api.measurements :as m]))

(defn online-sprees [] (:total (db/online {:spree true})))

(defn online-eatons [] (:total (db/online {:spree false})))

(defn offline-sprees [] (:total (db/offline {:spree true})))

(defn offline-eatons [] (:total (db/offline {:spree false})))

(defn current [] (:total (db/current nil)))

(defn outdated [] (:total (db/outdated nil)))

(defn account-timezone [timezone serial]
  (if (not (blank? timezone))
    timezone
    (:timezone (db/get-account-by-serial {:serial serial}))))

(defn account-attributes [settings serial]
  (let [measurement (m/first-measurement serial)]
    (merge (into {} {:spree_version (:spree_version settings)
                     :specified_capacity (:capacity_kw settings)
                     :specified_pv_capacity (:pvsize_kw settings)
                     :inverter_power (:inverter_power_kw settings)
                     :timezone (account-timezone (:timezone settings) serial)
                     :online true
                     :serial serial
                     :last_seen_at (time/local-date-time)}) measurement)))

(defn find-or-create-account!
  "Tries first to find an account and if not creates one and returns it"
  [serial]
  (if-let [account (db/get-account-by-serial {:serial serial})]
    account
    (if (db/get-machine-setting {:serial serial})
      (do
        (conman/with-transaction [db/*db*]
          (db/create-account! {:id serial
                               :serial serial
                               :spree_version 1}))
        (db/get-account-by-serial {:serial serial})))))

(defn update-account! [settings serial]
  (conman/with-transaction [db/*db*]
    (db/update-account! (account-attributes settings serial))))
