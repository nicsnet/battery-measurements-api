(ns battery-measurements-api.machine-settings
  (:require [conman.core :as conman]
            [taoensso.timbre :as timbre]
            [battery-measurements-api.db.core :as db]))

(defn find-machine-setting [serial]
  "Retrieves a machine setting for a given serial number"
  (db/get-machine-setting {:serial serial}))
