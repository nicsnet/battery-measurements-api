(ns battery-measurements-api.measurements
  (:require [clojure.set :refer [rename-keys]]
            [conman.core :as conman]
            [taoensso.timbre :as timbre]
            [battery-measurements-api.db.core :as db]))

(def db-columns
  {:discharge :m01
   :charge :m02
   :consumption :m03
   :production :m04
   :state_of_charge :m05})

(defn assign-charge-and-discharge [measurement]
  "Assigns the values for charge and discharge for a given measurement"
  (let [{charge :charge
         discharge :discharge} measurement]
    (assoc measurement :discharge (if (pos-int? discharge) discharge 0)
           :charge (if (neg-int? charge) (* -1 charge) 0))))

(defn convert-measurements [data serial]
  (->> data
       (map (fn [x] {:consumption (:Consumption_W x)
                    :production (:Production_W x)
                    :state_of_charge (:USOC x)
                    :timestamp (java.util.Date.)
                    :serial serial
                    :discharge (:Pac_total_W x)
                    :charge (:Pac_total_W x)} ))
       (map (fn [m] (assign-charge-and-discharge m)))
       (map #(rename-keys % db-columns))))

(defn create-measurements! [m s]
  (let [measurements (convert-measurements m s)]
    (println measurements)
  (conman/with-transaction [db/*db*]
    (timbre/info "Inserting into the measurements table")
    (->> measurements
         (map #(vec (map % [:serial :timestamp :m01 :m02 :m03 :m04 :m05])))
         vec
         (#(db/create-measurements! {:measurements %}))))))
