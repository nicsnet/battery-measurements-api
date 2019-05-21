CREATE TABLE `machine_status` (
  `serial` mediumint(8) unsigned NOT NULL DEFAULT '0',
  `key` varbinary(32) NOT NULL DEFAULT '',
  `version` mediumint(8) unsigned NOT NULL DEFAULT '0',
  `value` varbinary(256) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`serial`,`key`,`version`),
  KEY `key` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;