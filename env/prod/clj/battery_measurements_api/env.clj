(ns battery-measurements-api.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[battery-measurements-api started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[battery-measurements-api has shut down successfully]=-"))
   :middleware identity})
