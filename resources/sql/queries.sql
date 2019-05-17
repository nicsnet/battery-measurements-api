-- :name create-measurement! :! :n
-- :doc creates a new measurement record
INSERT INTO measurements
            (serial, timestamp, consumption, production, charge, discharge, state_of_charge)
VALUES (:serial, :timestamp, :consumption, :production, :charge, :discharge, :state_of_charge)
