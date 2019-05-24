(ns battery-measurements-api.measurements
  (:require [clojure.set :refer [rename-keys]]
            [clojure.walk :refer [postwalk-replace]]
            [conman.core :as conman]
            [taoensso.timbre :as timbre]
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

(def db-columns
  {:discharge :m01
   :charge :m02
   :consumption :m03
   :production :m04
   :state_of_charge :m05})


(defn convert-measurements [input-json]
  (->> input-json
       (map (fn [x] {:consumption (:Consumption_W x)
                    :production (:Production_W x)
                    :state_of_charge (:USOC x)
                    :timestamp (java.util.Date.)
                    :serial  (rand-int 10000)
                    :discharge (:Pac_total_W x)
                    :charge (:Pac_total_W x)} ))
       (map (fn [m] (assoc m :discharge (if (pos-int? (:discharge m)) (:discharge m) 0))))
       (map (fn [m] (assoc m :charge (if (neg-int? (:charge m)) (* -1 (:charge m))  0))))
       (map #(rename-keys % db-columns))))

(defn create-measurements [m]
  (let [measurements (convert-measurements m)]
    (println measurements)
  (conman/with-transaction [db/*db*]
    (timbre/info "Inserting into the measurements table")
    (->> measurements
         (map #(vec (map % [:serial :timestamp :m01 :m02 :m03 :m04 :m05])))
         vec
         (#(db/create-measurements! {:measurements %}))))))
