(ns battery-measurements-api.test.handler
  (:require
    [clojure.data.json :as json]
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [battery-measurements-api.handler :refer :all]
    [battery-measurements-api.middleware.formats :as formats]
    [battery-measurements-api.routes.services :refer [fetch-account process-data!]]
    [muuntaja.core :as m]
    [mount.core :as mount]))

(def post-sample-file
  "test/clj/battery_measurements_api/test/spree_post_sample.json")

(def post-sample
  (json/read-str (slurp post-sample-file) :key-fn keyword))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'battery-measurements-api.config/env
                 #'battery-measurements-api.handler/app)
    (f)))

(deftest test-app
  (testing "swagger documentation route"
    (let [response (app (request :get "/havel/api-docs/index.html"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response)))))

  (testing "services"
    (testing "success"
      (with-redefs [fetch-account (fn [_] {:serial 2222})
                    process-data! (fn [_ _ _] {:status 200 :body {:total 16}})]
        (let [response (app (-> (request :post "/havel/units/2222/operating_data")
                                (json-body post-sample)))]
          (is (= 200 (:status response)))
          (is (= {:total 16} (m/decode-response-body response))))))

    (testing "parameter coercion error"
      (with-redefs [fetch-account (fn [_] {:serial 2222})
                    process-data! (fn [_ _ _] {:status 200 :body {:total 16}})]
        (let [response (app (-> (request :post "/havel/units/2222/operating_data")
                                (json-body {:x 10, :y "invalid"})))]
          (is (= 400 (:status response))))))

    (testing "response coercion error"
      (let [response (app (-> (request :post "/havel/units/2222/operating_data")
                              (json-body {:x -10, :y 6})))]
        (is (= 400 (:status response)))))))
