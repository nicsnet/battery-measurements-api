(ns battery-measurements-api.db.helpers)

(defn ignored-ips []
  "('178.19.214.131',
   '185.19.198.179',
   '213.182.26.2',
   '24.134.68.117',
   '24.134.7.229',
   '58.171.8.1',
   '61.88.9.98',
   '73.207.249.142',
   '75.52.246.30',
   '96.89.71.241',
   '96.89.71.243')")

(defn excluded-us-devices []
  "(select distinct serial from machine_settings where `key` = 'update_channel' and `value` = 'us-stable')")
