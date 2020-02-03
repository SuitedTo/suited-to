# --- !Ups

CREATE TABLE PREP_Client
(
	id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    created DATETIME,
    updated DATETIME,
    client_id VARCHAR(255) NOT NULL,
    client_secret VARCHAR(255) NOT NULL
);

CREATE TABLE PREP_ClientSession
(
	id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    created DATETIME,
    updated DATETIME,
    client_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    FOREIGN KEY ( user_id ) REFERENCES PREP_User ( id ),
    FOREIGN KEY ( client_id ) REFERENCES PREP_Client ( id )
);

DROP TABLE PREP_Session;

# --- !Downs

DROP TABLE PREP_ClientSession;

DROP TABLE PREP_Client;


CREATE TABLE `PREP_Session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKAC0BDE643DA00BE4` (`user_id`),
  CONSTRAINT `FKAC0BDE643DA00BE4` FOREIGN KEY (`user_id`) REFERENCES `PREP_User` (`id`)
);

