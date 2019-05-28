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

-- :name create-cellpack-data! :! :n
-- :doc creates a number of new cellpack data records
insert into cellpack_data
            (serial,
            timestamp,
            module_id,
            system_time,
            system_status,
            system_alarm,
            system_warning,
            charge_current_limit,
            discharge_current_limit,
            system_current,
            system_avg_current,
            max_module_current,
            min_module_current,
            system_dc_voltage,
            max_module_dc_voltage,
            min_module_dc_voltage,
            max_cell_voltage,
            min_cell_voltage,
            max_cell_temp,
            min_cell_temp,
            rsoc,
            remaining_capacity,
            full_charge_capacity,
            soh,
            cycle_count)
values :t*:cellpack_data
