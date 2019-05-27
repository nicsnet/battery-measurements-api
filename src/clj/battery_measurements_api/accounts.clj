(ns battery-measurements-api.accounts
  (:require [conman.core :as conman]
            [taoensso.timbre :as timbre]
            [battery-measurements-api.db.core :as db]))

(defn find-or-create-account! [serial]
  "Tries first to find an account and if not creates one and returns the serial"
  (if-let [account (db/get-account {:serial serial})]
    serial
    (if-let [machine_setting (db/get-machine-setting {:serial serial})]
      (do
        (conman/with-transaction [db/*db*]
          (timbre/info "Creating new account")
          (db/create-account! {:id serial
                               :serial serial
                               :spree_version 1}))
        serial))))
