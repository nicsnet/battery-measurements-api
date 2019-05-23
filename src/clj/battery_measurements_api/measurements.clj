(ns battery-measurements-api.measurements
  (:require [clojure.java.jdbc :as jdbc]
            [conman.core :as conman]
            [battery-measurements-api.db.core :as db]))

(defn create-measurement [measurement]
  (conman/with-transaction [db/*db*]
    (db/create-measurement! {:serial 111
                             :timestamp (java.util.Date.)
                             :discharge (:Pac_total_W measurement)
                             :charge (:Pac_total_W measurement)
                             :consumption (:Consumption_W measurement)
                             :production (:Production_W measurement)
                             :state_of_charge (:USOC measurement)})))
