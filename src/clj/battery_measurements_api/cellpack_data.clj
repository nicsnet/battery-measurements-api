(ns battery-measurements-api.cellpack-data
  (:require [conman.core :as conman]
            [java-time :as time]
            [taoensso.timbre :as timbre]
            [battery-measurements-api.db.core :as db]))

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

(defn convert-cellpack-data [rows serial]
  (let [cellpack-data (map :bms_sony rows)
        timestamp (str->timestamp (first (map :timestamp rows)))]
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
                      :timestamp timestamp})))))

(defn create-cellpack-data! [data serial]
  (let [cellpack-data (convert-cellpack-data data serial)]
    (conman/with-transaction [db/*db*]
      (timbre/info "Inserting into the cellpack data table for serial" serial)
      (->> cellpack-data
           (map #(vec (map % db-columns)))
           vec
           (#(db/create-cellpack-data! {:cellpack_data %}))))))
