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
-- :doc count the number of currently online eaton batteries
select count(*) as total from accounts
 where
   /*~ (if (:spree params) */
   last_seen_at > UTC_TIMESTAMP() - interval 15 minute
   and (spree_version > 0 or hw_version >= 8)
   /*~*/
   last_seen_at > UTC_TIMESTAMP() - interval 12 hour
   and spree_version = 0
   /*~ ) ~*/
   :snip:exclude-ip-addresses
   and serial not in (:snip:exclude-devices)

-- :name offline :? :1
-- :doc count the number of currently online eaton batteries
select count(*) as total from accounts
 where
   /*~ (if (:spree params) */
   last_seen_at < UTC_TIMESTAMP() - interval 15 minute
   and (spree_version > 0 or hw_version >= 8)
   /*~*/
   (last_seen_at < UTC_TIMESTAMP() - interval 12 hour or last_seen_at is null)
   and spree_version = 0
   /*~ ) ~*/
   :snip:exclude-ip-addresses
   and serial not in (:snip:exclude-devices)

-- :snip us-devices
select distinct serial from machine_settings
 where `key` = 'update_channel' and `value` = 'us-stable'

-- :snip ignored-ips
and wan_ip not in ('178.19.214.131',
                   '185.19.198.179',
                   '213.182.26.2',
                   '24.134.68.117',
                   '24.134.7.229',
                   '58.171.8.1',
                   '61.88.9.98',
                   '73.207.249.142',
                   '75.52.246.30',
                   '96.89.71.241',
                   '96.89.71.243')

-- :snip eaton
and hw_version < 8

-- :snip spree
and (spree_version > 0 or hw_version >= 8)


