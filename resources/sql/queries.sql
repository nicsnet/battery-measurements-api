-- :name create-measurements! :! :n
-- :doc creates a number of new measurement records
insert into measurements (serial, timestamp, m01, m02, m03, m04, m05)
  values :t*:measurements

-- :name get-account :? :1
-- :doc retrieve an account given the serial number
select * from accounts
  where serial = :serial

-- :name create-account! :! :n
-- :doc creates a new account record
insert into accounts
  (id, serial, spree_version, created_at, updated_at)
values (:id, :serial, :spree_version, current_time(), current_time())

-- :name get-machine-setting :? :1
-- :doc retrieve a machine setting given the serial number
select * from machine_settings
  where serial = :serial
