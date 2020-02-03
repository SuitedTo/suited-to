# --- !Ups

CREATE TABLE `PREP_User` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `userId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);


CREATE TABLE `PREP_Session` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKAC0BDE643DA00BE4` (`user_id`),
  CONSTRAINT `FKAC0BDE643DA00BE4` FOREIGN KEY (`user_id`) REFERENCES `PREP_User` (`id`)
);




# --- !Downs

DROP TABLE PREP_Session;
DROP TABLE PREP_User;

