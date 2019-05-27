(ns battery-measurements-api.accounts
  (:require [conman.core :as conman]
            [taoensso.timbre :as timbre]
            [battery-measurements-api.db.core :as db]))

(defn find-account [serial]
  "Retrieves an account for a given serial number"
  (db/get-account {:serial serial}))
