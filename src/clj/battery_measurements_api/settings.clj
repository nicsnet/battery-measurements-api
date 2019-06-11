(ns battery-measurements-api.settings
  (:require [clojure.set :refer [rename-keys]]
            [conman.core :as conman]
            [java-time :as time]
            [taoensso.timbre :as timbre]
            [battery-measurements-api.accounts :as a]
            [battery-measurements-api.db.core :as db]))

(defn first-measurement [serial]
  (db/first-measurement {:serial serial}))

(defn account-timezone [serial]
  (:timezone (db/get-account {:serial serial})))

(defn acccount-attributes [settings serial]
  (let [measurement (first-measurement serial)]
    (merge (into {} {:spree_version (:spree_version settings)
                     :specified_capacity (:capacity_kw settings)
                     :specified_pv_capacity (:pvsize_kw settings)
                     :inverter_power (:inverter_power_kw settings)
                     :timezone (account-timezone serial)
                     :online true
                     :serial serial
                     :last_seen_at (time/local-date-time)}) measurement)))

(defn machine-status-attributes [settings serial]
  (let [measurement (first-measurement serial)]
    (into {} {:spree_version (:spree_version settings)
              :InstalledPvPower (:pvsize_kw settings)
              :Capacity (:capacity_kw settings)
              :MaxFeedIn (:maxfeedin_percent settings)
              :MaxInverterPower (:inverter_power_kw settings)
              :marketing_module_capacity (:marketing_module_capacity settings)
              :last_connection (inst-ms (time/instant))
              :SOC (:state_of_charge measurement)
              :M30 (:state_of_charge measurement)})))

(defn convert-machine-statuses [settings serial]
  (->>(machine-status-attributes settings serial)
      vec
      (map (fn [m] {:key (name (first m))
                   :value (second m)
                   :serial serial
                   :version 0
                   :created_at (time/local-date-time)
                   :updated_at (time/local-date-time)}))))

(defn create-machine-statuses! [data serial]
  (let [machine-statuses (convert-machine-statuses data serial)]
    (conman/with-transaction [db/*db*]
      (timbre/info "Inserting into the machine status table for serial" serial)
      (->> machine-statuses
           (map #(vec (map % [:serial :key :value :version :created_at :updated_at])))
           vec
            (#(db/create-machine-statuses! {:machine_status %}))))))
