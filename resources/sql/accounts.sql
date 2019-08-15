-- :name get-account-by-serial :? :1
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

-- :name online :? :1
-- :require [battery-measurements-api.db.helpers :refer [ignored-ips excluded-us-devices]]
-- :doc count the number of currently online batteries
-- You can pass {:spree true} to check for spree batteries and {:spree false} for eatons
select count(*) as total from accounts
  where serial not in /*~ (excluded-us-devices) ~*/
    /*~ (if (:spree params) */
    and last_seen_at > UTC_TIMESTAMP() - interval 15 minute and (spree_version > 0 or hw_version >= 8)
    /*~*/
    and (last_seen_at > UTC_TIMESTAMP() - interval 12 hour) and spree_version = 0
    /*~ ) ~*/
    and wan_ip not in /*~ (ignored-ips) ~*/

-- :name offline :? :1
-- :require [battery-measurements-api.db.helpers :refer [ignored-ips excluded-us-devices]]
-- :doc count the number of currently offline batteries
-- You can pass {:spree true} to check for spree batteries and {:spree false} for eatons
select count(*) as total from accounts
  where serial not in /*~ (excluded-us-devices) ~*/
    /*~ (if (:spree params) */
    and (last_seen_at < UTC_TIMESTAMP() - interval 15 minute) and (spree_version > 0 or hw_version >= 8)
    /*~*/
    and (last_seen_at < UTC_TIMESTAMP() - interval 12 hour or last_seen_at is null) and spree_version = 0
    /*~ ) ~*/
    and wan_ip not in /*~ (ignored-ips) ~*/
