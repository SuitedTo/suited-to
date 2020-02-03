# --- !Ups

CREATE TABLE SocialIdentity (
  id bigint(20) NOT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  accessToken varchar(255) DEFAULT NULL,
  accessTokenURL varchar(255) DEFAULT NULL,
  authenticationMethod int(11) DEFAULT NULL,
  authorizationURL varchar(255) DEFAULT NULL,
  avatarUrl varchar(255) DEFAULT NULL,
  consumerKey varchar(255) DEFAULT NULL,
  consumerSecret varchar(255) DEFAULT NULL,
  displayName varchar(255) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  externalId varchar(255) NOT NULL,
  lastAccess datetime DEFAULT NULL,
  provider int(11) NOT NULL,
  requestTokenURL varchar(255) DEFAULT NULL,
  secret varchar(255) DEFAULT NULL,
  token varchar(255) DEFAULT NULL,
  user_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  KEY FKC9ADFB6B47140EFE (user_id),
  CONSTRAINT FKC9ADFB6B47140EFE FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE TABLE Alert (
  id bigint(20) NOT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  message varchar(255) DEFAULT NULL,
  type int(11) DEFAULT NULL,
  user_id bigint(20) DEFAULT NULL,
  acknowledged datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK3C6993C47140EFE (user_id),
  CONSTRAINT FK3C6993C47140EFE FOREIGN KEY (user_id) REFERENCES app_user (id)
);


# --- !Downs

DROP TABLE SocialIdentity;

DROP TABLE Alert;
