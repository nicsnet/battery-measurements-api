(ns battery-measurements-api.accounts
  (:require [conman.core :as conman]
            [java-time :as time]
            [taoensso.timbre :as timbre]
            [battery-measurements-api.db.core :as db]
            [battery-measurements-api.measurements :as m]))

(def excluded {:exclude-devices (db/us-devices) :exclude-ip-addresses (db/ignored-ips)})

(def spree-params (merge excluded {:spree true}))

(def eaton-params (merge excluded {:spree false}))

(defn online-sprees [] (:total (db/online spree-params)))

(defn online-eatons [] (:total (db/online eaton-params)))

(defn offline-sprees [] (:total (db/offline spree-params)))

(defn offline-eatons [] (:total (db/offline eaton-params)))

(defn account-timezone [serial]
  (:timezone (db/get-account-by-serial {:serial serial})))

(defn acccount-attributes [settings serial]
  (let [measurement (m/first-measurement serial)]
    (merge (into {} {:spree_version (:spree_version settings)
                     :specified_capacity (:capacity_kw settings)
                     :specified_pv_capacity (:pvsize_kw settings)
                     :inverter_power (:inverter_power_kw settings)
                     :timezone (account-timezone serial)
                     :online true
                     :serial serial
                     :last_seen_at (time/local-date-time)}) measurement)))

(defn find-or-create-account! [serial]
  "Tries first to find an account and if not creates one and returns it"
  (if-let [account (db/get-account-by-serial {:serial serial})]
    account
    (if-let [machine_setting (db/get-machine-setting {:serial serial})]
      (do
        (conman/with-transaction [db/*db*]
          (timbre/info "Creating new account")
          (db/create-account! {:id serial
                               :serial serial
                               :spree_version 1}))
          (db/get-account-by-serial {:serial serial})))))

(defn update-account! [settings serial]
  (conman/with-transaction [db/*db*]
    (timbre/info "Update account record for serial" serial)
    (db/update-account! (acccount-attributes settings serial))))
