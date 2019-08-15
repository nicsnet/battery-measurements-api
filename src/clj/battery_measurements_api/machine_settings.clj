(ns battery-measurements-api.machine-settings
  (:require [battery-measurements-api.db.core :as db]))

(defn find-machine-setting
  "Retrieves a machine setting for a given serial number"
  [serial]
  (db/get-machine-setting {:serial serial}))
