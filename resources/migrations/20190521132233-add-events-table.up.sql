CREATE TABLE `events` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `timestamp` datetime NOT NULL DEFAULT '1970-01-01 00:00:00',
  `serial` mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT 'PSB serial number',
  `code` mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT 'Error/Event code number',
  `entity_id` varchar(255) DEFAULT NULL COMMENT 'Entity ID if any, e.g. cell id, bms id, ...',
  `old_value` varchar(255) DEFAULT NULL,
  `new_value` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `alert_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`,`serial`,`timestamp`,`code`),
  KEY `alert_id` (`alert_id`),
  KEY `serial_timestamp` (`serial`,`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;