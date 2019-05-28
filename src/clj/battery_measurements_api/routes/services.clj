(ns battery-measurements-api.routes.services
  (:require
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.ring.coercion :as coercion]
   [reitit.coercion.spec :as spec-coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]
   [ring.util.http-response :refer :all]
   [taoensso.timbre :as timbre]
   [battery-measurements-api.middleware.formats :as formats]
   [battery-measurements-api.middleware.exception :as exception]
   [battery-measurements-api.accounts :as a]
   [battery-measurements-api.measurements :as m]))

(def settings
  {:inverter_power_kw any?
   :pvsize_kw any?
   :marketing_module_capacity int?
   :maxfeedin_percent int?
   :capacity_kw int?
   :spree_version int?
   :timezone string?
   :TimezoneOffset int?})

(def measurements
  {:Consumption_W int?
   :Pac_total_W int?
   :USOC int?
   :Production_W int?})

(def rows
  [{:bms_sony {:CC int?
               :CCL_mA int?
               :DCL_mA int?
               :FFC_mAh int?
               :MAXCV_mV int?
               :MAXCT_0.1K int?
               :MAXMDCV_mV int?
               :MAXMC_mA int?
               :MINMDCV_mV int?
               :MINMC_mA int?
               :MINCT_0.1K int?
               :MINCV_mV int?
               :ModId int?
               :RC_mAh int?
               :RSOC_0.1% int?
               :SA int?
               :SAC_mA int?
               :SC_mA int?
               :SDCV_mV int?
               :SOH_0.1% int?
               :SS int?
               :ST_sec int?
               :SW int?}

    :id int?
    :timestamp string?
    :measurements measurements}])

(def operating-data {:settings settings
                     :rows rows})

(defn create-measurements! [rows serial]
  (if-let [account-serial (a/find-or-create-account! serial)]
    (do (m/create-measurements! rows account-serial)
        {:status 200 :body {:my-int rows}})
    (not-found)))

(defn operating-data-handler [{{path :path {:keys [settings rows]} :body} :parameters}]
  (let [serial (:unit-serial path)]
    (create-measurements! rows serial)))

(defn service-routes []
  ["/havel"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
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
             :parameters {:body operating-data :path {:unit-serial int?}}
             :responses {200 {:body any?}
                         404 {:description "Account for serial not found"}}
             :handler operating-data-handler}}]]])
