-- :name create-measurement! :! :n
-- :doc creates a new measurement record
insert into measurements (serial, timestamp, m01, m02, m03, m04, m05)
values (:serial, :timestamp, :discharge, :charge, :production, :consumption, :state_of_charge)

-- :name create-measurements! :! :n
-- :doc creates a number of new measurement records
INSERT INTO measurements (serial, timestamp, m01, m02, m03, m04, m05)
VALUES :t*:measurements
