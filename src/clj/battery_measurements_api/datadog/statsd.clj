(ns battery-measurements-api.datadog.statsd
  (:require [battery-measurements-api.accounts :refer [online-eatons
                                                       online-sprees
                                                       offline-eatons
                                                       offline-sprees]]
            [battery-measurements-api.config :refer [env]]
            [clojure.core.async :refer [go <! timeout close!]]
            [cognician.dogstatsd :as dogstatsd]
            [mount.core :as mount]))

;; Time in ms for the intervals of sending metrics to datadog
(def interval-in-ms 120000)

(defn send-datadog-metrics []
  "Sends metrics to the datadog agent with the totals of currently online/offline batteries"
  (dogstatsd/configure! (env :datadog-agent-url) {:tags {:env env}})
  (go (loop [] (<! (timeout interval-in-ms))
            (do
              (dogstatsd/gauge! "core.battery.eaton.online" (online-eatons))
              (dogstatsd/gauge! "core.battery.eaton.offline" (offline-eatons))
              (dogstatsd/gauge! "core.battery.spree.offline" (offline-sprees))
              (dogstatsd/gauge! "core.battery.spree.online" (online-sprees)))
            (recur))))

(mount/defstate ^:dynamic datadog-agent
  :start (send-datadog-metrics)
  :stop (close! datadog-agent))
