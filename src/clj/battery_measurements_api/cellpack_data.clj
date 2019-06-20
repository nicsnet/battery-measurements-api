(ns battery-measurements-api.cellpack-data
  (:require [clojure.spec.alpha :as s]
            [conman.core :as conman]
            [java-time :as time]
            [taoensso.timbre :as timbre]
            [battery-measurements-api.db.core :as db]))

(s/def ::CC int?)
(s/def ::CCL_mA int?)
(s/def ::DCL_mA int?)
(s/def ::FFC_mAh int?)
(s/def ::MAXCV_mV int?) ;; Maximum Module DC Voltage
(s/def ::MAXCT_0.1K int?)
(s/def ::MAXMDCV_mV int?)
(s/def ::MAXMC_mA int?)
(s/def ::MINMDCV_mV int?)
(s/def ::MINMC_mA int?)
(s/def ::MINCT_0.1K int?)
(s/def ::MINCV_mV int?)
(s/def ::ModId int?)
(s/def ::RC_mAh int?)
(s/def ::RSOC_0.1% int?)
(s/def ::SA int?)
(s/def ::SAC_mA int?)
(s/def ::SC_mA int?)
(s/def ::SDCV_mV int?)
(s/def ::SOH_0.1% int?)
(s/def ::SS int?)
(s/def ::ST_sec int?)
(s/def ::SW int?)

(s/def ::bms_sony (s/keys :req-un [::CC
                                  ::CCL_mA
                                  ::DCL_mA
                                  ::FFC_mAh
                                  ::MAXCV_mV
                                  ::MAXCT_0.1K
                                  ::MAXMDCV_mV
                                  ::MAXMC_mA
                                  ::MINMDCV_mV
                                  ::MINMC_mA
                                  ::MINCT_0.1K
                                  ::MINCV_mV
                                  ::ModId
                                  ::RC_mAh
                                  ::RSOC_0.1%
                                  ::SA
                                  ::SAC_mA
                                  ::SC_mA
                                  ::SDCV_mV
                                  ::SOH_0.1%
                                  ::SS
                                  ::ST_sec
                                  ::SW]))

(def timestamp-format "yyyy-MM-dd HH:mm:ss")

(defn str->timestamp [t]
  (time/local-date-time timestamp-format t))

(def db-columns
  [:serial
   :timestamp
   :module_id
   :system_time
   :system_status
   :system_alarm
   :system_warning
   :charge_current_limit
   :discharge_current_limit
   :system_current
   :system_avg_current
   :max_module_current
   :min_module_current
   :system_dc_voltage
   :max_module_dc_voltage
   :min_module_dc_voltage
   :max_cell_voltage
   :min_cell_voltage
   :max_cell_temp
   :min_cell_temp
   :rsoc
   :remaining_capacity
   :full_charge_capacity
   :soh
   :cycle_count])

(defn merge-timestamps [rows]
  (->> rows
       (map #(update-in % [:bms_sony] merge {:timestamp (:timestamp %)}))
       (map :bms_sony)))

(defn convert-cellpack-data [rows serial]
  (let [cellpack-data (merge-timestamps rows)]
    (->> cellpack-data
         (map (fn [x] {:charge_current_limit (:CCL_mA x)
                      :cycle_count (:CC x)
                      :discharge_current_limit (:DCL_mA x)
                      :full_charge_capacity (:FFC_mAh x)
                      :max_cell_voltage (:MAXCV_mV x)
                      :max_module_current (:MAXMC_mA x)
                      :max_module_dc_voltage (:MAXMDCV_mV x)
                      :min_cell_temp (:MINCT_0.1K x)
                      :min_cell_voltage (:MINCV_mV x)
                      :max_cell_temp (:MAXCT_0.1K x)
                      :min_module_current (:MINMC_mA x)
                      :min_module_dc_voltage (:MINMDCV_mV x)
                      :module_id (:ModId x)
                      :rsoc (:RSOC_0.1% x)
                      :remaining_capacity (:RC_mAh x)
                      :serial serial
                      :soh (:SOH_0.1% x)
                      :system_avg_current (:SAC_mA x)
                      :system_current (:SC_mA x)
                      :system_dc_voltage (:SDCV_mV x)
                      :system_time (:ST_sec x)
                      :system_status (:SS x)
                      :system_alarm (:SA x)
                      :system_warning (:SW x)
                      :timestamp (str->timestamp (:timestamp x))})))))

(defn create-cellpack-data! [data serial]
  (let [cellpack-data (convert-cellpack-data data serial)]
    (conman/with-transaction [db/*db*]
      (timbre/info "Inserting into the cellpack data table for serial" serial)
      (->> cellpack-data
           (map #(vec (map % db-columns)))
           vec
           (#(db/create-cellpack-data! {:cellpack_data %}))))))
