-- :name create-measurements! :! :n
-- :doc creates a number of new measurement records
insert ignore into measurements (serial, timestamp, m01, m02, m03, m04, m05)
  values :t*:measurements

-- :name first-measurement :? :1
-- :doc select the first measurement for an account given the serial
select m01 as discharge,
       m02 as charge,
       m03 as consumption,
       m04 as production,
       m05 as state_of_charge,
       timestamp as measured_at
  from measurements join accounts
 where measurements.serial = accounts.serial
   and measurements.serial = :serial
 order by timestamp desc limit 1


-- :name get-account :? :1
-- :doc retrieve an account given the serial number
select * from accounts
  where serial = :serial

-- :name create-account! :! :n
-- :doc creates a new account record
insert into accounts
  (id, serial, spree_version, created_at, updated_at)
values (:id, :serial, :spree_version, current_time, current_time)

-- :name update-account! :! :n
-- :doc update an existing account record
update accounts
   set spree_version = :spree_version,
       specified_capacity = :specified_capacity,
       specified_pv_capacity = :specified_pv_capacity,
       inverter_power = :inverter_power,
       timezone = :timezone,
       online = :online,
       last_seen_at = :last_seen_at,
       measured_at = :measured_at,
       production = :production,
       charge = :charge,
       discharge = :discharge,
       consumption = :consumption,
       state_of_charge = :state_of_charge
where serial = :serial

-- :name get-machine-setting :? :1
-- :doc retrieve a machine setting given the serial number
select * from machine_settings
 where serial = :serial

-- :name create-machine-setting! :! :n
-- :doc creates a new machine setting record
insert into machine_settings (serial, `key`, `value`, version, created_at, updated_at)
  value (:serial, :key, :value, :version, current_time, current_time)

-- :name create-machine-statuses! :! :n
-- :doc creates a number of new machine status records
insert into machine_status (serial, `key`, `value`, version, created_at, updated_at)
  values :t*:machine_status
  on duplicate key update updated_at = current_time()

-- :name create-machine-status! :! :n
-- :doc creates a new machine status record
insert into machine_status (serial, `key`, `value`, version, created_at, updated_at)
  value (:serial, :key, :value, :version, current_time, current_time)

-- :name create-cellpack-data! :! :n
-- :doc creates a number of new cellpack data records
insert ignore into cellpack_data
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
