(ns battery-measurements-api.routes.services
  (:require [clojure.spec.alpha :as s]
            [reitit.coercion.spec :as spec-coercion]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [ring.util.http-response :refer :all]
            [taoensso.timbre :as timbre]
            [battery-measurements-api.middleware.formats :as formats]
            [battery-measurements-api.middleware.exception :as exception]
            [battery-measurements-api.accounts :as accounts]
            [battery-measurements-api.cellpack-data :as cellpack-data]
            [battery-measurements-api.measurements :as measurements]
            [battery-measurements-api.settings :as settings]))


(s/def ::import-response (s/keys))
(s/def ::id int?)
(s/def ::timestamp string?)
(s/def ::cellpack-data (s/keys :req-un [::cellpack-data/bms_sony
                               ::timestamp
                               ::id
                               ::measurements/measurements]))

(s/def ::rows (s/coll-of ::cellpack-data))
(s/def ::operating-data (s/keys :req-un [::settings/settings ::rows]))

(defn fetch-account [serial]
  (accounts/find-or-create-account! serial))

(def response-code-mapping {:failure 0 :success 2})

(defn response-codes [rows type]
  "Returns a map with ids as keys and values for success or failure codes"
  (let [code (type response-code-mapping)
        ids (remove #(nil? %) (map :id rows))]
    (zipmap ids (repeat code))))

(defn process-data! [settings rows serial]
  (if-let [account-serial (:serial (fetch-account serial))]
    (try
      (do (measurements/create-measurements! rows account-serial)
          (cellpack-data/create-cellpack-data! rows account-serial)
          (settings/create-machine-statuses! settings account-serial)
          (accounts/update-account! settings account-serial)
          (ok (response-codes rows :success)))
      (catch Exception e
        (timbre/log :error e)
        (bad-request (response-codes rows :failure))))
    (not-found)))

(defn operating-data-handler [{{path :path {:keys [settings rows]} :body} :parameters}]
  (let [serial (:unit-serial path)]
    (process-data! settings rows serial)))

(defn service-routes []
  ["/havel"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :exception pretty/exception
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "battery-measurements-api"
                         :description "Api to store spree battery measurements in the sonnen db"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url "/havel/swagger.json"
              :config {:validator-url nil}})}]]

   ["/units/:unit-serial"
    {:swagger {:tags ["operating_data"]}}

    ["/operating_data"
     {:post {:summary "Create new measurements"
             :parameters {:body ::operating-data :path {:unit-serial int?}}
             :responses {200 {:body ::import-response}
                         404 {:description "Account for serial not found"}}
             :handler operating-data-handler}}]]])
