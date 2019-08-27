(ns battery-measurements-api.test.measurements-test
  (:require
   [battery-measurements-api.measurements :as m]
   [clojure.test :refer [is deftest testing]]))

(deftest test-measurements
  (testing "assinging charge and discharge"
    (is (= {:charge 2 :discharge 0} (m/assign-charge-and-discharge {:charge -2 :discharge -3})))
    (is (= {:charge 0 :discharge 0} (m/assign-charge-and-discharge {:charge 2 :discharge -3})))
    (is (= {:charge 0 :discharge 3} (m/assign-charge-and-discharge {:charge 2 :discharge 3})))
    (is (= {:charge 2 :discharge 3} (m/assign-charge-and-discharge {:charge -2 :discharge 3})))))
