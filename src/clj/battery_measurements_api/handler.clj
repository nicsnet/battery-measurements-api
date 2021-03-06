(ns battery-measurements-api.handler
  (:require
    [battery-measurements-api.middleware :as middleware]
    [battery-measurements-api.routes.services :refer [service-routes]]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [battery-measurements-api.env :refer [defaults]]
    [mount.core :as mount]))

(defn health-check [_]
  {:status 200, :body "ok"})

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(mount/defstate app
  :start
  (middleware/wrap-base
    (ring/ring-handler
      (ring/router
       [["/health" {:get health-check}]
        ["/" {:get
               {:handler (constantly {:status 301 :headers {"Location" "/havel/api-docs/index.html"}})}}]
         (service-routes)])
      (ring/routes
        (ring/create-resource-handler
          {:path "/"})
        (wrap-content-type (wrap-webjars (constantly nil)))
        (ring/create-default-handler)))))
