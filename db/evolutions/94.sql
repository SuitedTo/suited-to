# --- !Ups

DROP TABLE DataTrigger;

# --- !Downs

CREATE TABLE `DataTrigger` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `triggerKey` varchar(255) DEFAULT NULL,
  `propertyName` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `domain` varchar(255) DEFAULT NULL,
  `subdomain` varchar(255) DEFAULT NULL,
  `targetValue` varchar(255) DEFAULT NULL,
  `active` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_domain` (`triggerKey`,`domain`)
);