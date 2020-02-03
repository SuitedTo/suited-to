# --- !Ups

ALTER TABLE Category ADD COLUMN isAvailableExternally bit(1) DEFAULT b'0';
UPDATE Category SET isAvailableExternally = 0;

CREATE TABLE `PREP_Category` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `categoryId` varchar(255) NOT NULL,
  `isPrepSearchable` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id`)
);


# --- !Downs
DROP TABLE PREP_Category;

ALTER TABLE Category DROP COLUMN isAvailableExternally;