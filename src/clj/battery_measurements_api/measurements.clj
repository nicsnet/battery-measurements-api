(ns battery-measurements-api.measurements
  (:require [clojure.set :refer [rename-keys]]
            [clojure.spec.alpha :as s]
            [conman.core :as conman]
            [java-time :as time]
            [taoensso.timbre :as timbre]
            [battery-measurements-api.db.core :as db]))

(s/def ::Consumption_W int?)
(s/def ::Pac_total_W int?)
(s/def ::Production_W int?)
(s/def ::USOC int?)

(s/def ::measurements (s/keys :req-un [::Consumption_W
                                       ::Pac_total_W
                                       ::Production_W
                                       ::USOC]))

(def timestamp-format "yyyy-MM-dd HH:mm:ss")

(defn str->timestamp [t]
  (time/local-date-time timestamp-format t))

(defn first-measurement [serial]
  (db/first-measurement {:serial serial}))

(def db-columns
  {:discharge :m01
   :charge :m02
   :consumption :m03
   :production :m04
   :state_of_charge :m05})

(defn assign-charge-and-discharge
  "Assigns the values for charge and discharge for a given measurement"
  [measurement]
  (let [{charge :charge
         discharge :discharge} measurement]
    (assoc measurement :discharge (if (pos-int? discharge) discharge 0)
           :charge (if (neg-int? charge) (* -1 charge) 0))))

(defn merge-timestamps [rows]
  (->> rows
       (map #(update-in % [:measurements] merge {:timestamp (:timestamp %)}))
       (map :measurements)))

(defn convert-measurements [rows serial]
  (let [measurements (merge-timestamps rows)]
  (->> measurements
       (map (fn [x] {:consumption (:Consumption_W x)
                    :production (:Production_W x)
                    :state_of_charge (:USOC x)
                    :serial serial
                    :timestamp (str->timestamp (:timestamp x))
                    :discharge (:Pac_total_W x)
                    :charge (:Pac_total_W x)} ))
       (map (fn [m] (assign-charge-and-discharge m)))
       (map #(rename-keys % db-columns)))))

(defn create-measurements! [data serial]
  (let [measurements (convert-measurements data serial)]
    (conman/with-transaction [db/*db*]
      (timbre/info "Inserting into the measurements table for serial" serial)
      (->> measurements
           (map #(vec (map % [:serial :timestamp :m01 :m02 :m03 :m04 :m05])))
           vec
           (#(db/create-measurements! {:measurements %}))))))
