(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
    [battery-measurements-api.config :refer [env]]
    [clojure.spec.alpha :as s]
    [expound.alpha :as expound]
    [mount.core :as mount]
    [battery-measurements-api.core :refer [start-app]]
    [battery-measurements-api.db.core]
    [conman.core :as conman]
    [luminus-migrations.core :as migrations]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start 
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'battery-measurements-api.core/repl-server))

(defn stop 
  "Stops application."
  []
  (mount/stop-except #'battery-measurements-api.core/repl-server))

(defn restart 
  "Restarts application."
  []
  (stop)
  (start))

(defn restart-db 
  "Restarts database."
  []
  (mount/stop #'battery-measurements-api.db.core/*db*)
  (mount/start #'battery-measurements-api.db.core/*db*)
  (binding [*ns* 'battery-measurements-api.db.core]
    (conman/bind-connection battery-measurements-api.db.core/*db* "sql/queries.sql")))

(defn reset-db 
  "Resets database."
  []
  (migrations/migrate ["reset"] (select-keys env [:database-url])))

(defn migrate 
  "Migrates database up for all outstanding migrations."
  []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback 
  "Rollback latest database migration."
  []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration 
  "Create a new up and down migration file with a generated timestamp and `name`."
  [name]
  (migrations/create name (select-keys env [:database-url])))


