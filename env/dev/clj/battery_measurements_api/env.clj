(ns battery-measurements-api.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [battery-measurements-api.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[battery-measurements-api started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[battery-measurements-api has shut down successfully]=-"))
   :middleware wrap-dev})
