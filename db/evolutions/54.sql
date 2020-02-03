
# --- !Ups

CREATE TABLE ID_SEQ (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  value bigint(20) NOT NULL,
  PRIMARY KEY (id)
);

-- use 10000000 to ensure that it's higher than any existing value
insert into ID_SEQ (name, value) values ('STANDARD_ENTITY', 300000);

CREATE TABLE OAuthServiceProvider (
  id bigint(20) NOT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  accessTokenURL varchar(255) DEFAULT NULL,
  baseURL varchar(255) DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  requestTokenURL varchar(255) DEFAULT NULL,
  userAuthorizationURL varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE OAuthConsumer (
  id bigint(20) NOT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  callbackURL varchar(255) DEFAULT NULL,
  consumerKey varchar(255) DEFAULT NULL,
  consumerSecret varchar(255) DEFAULT NULL,
  description varchar(255) DEFAULT NULL,
  httpMethod varchar(255) DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  signatureMethod varchar(255) DEFAULT NULL,
  x509Certificate varchar(255) DEFAULT NULL,
  serviceProvider_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK6A26F3AD3351E987 (serviceProvider_id),
  CONSTRAINT FK6A26F3AD3351E987 FOREIGN KEY (serviceProvider_id) REFERENCES OAuthServiceProvider (id)
);


CREATE TABLE OAuthAccessor (
  id bigint(20) NOT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  accessToken varchar(255) DEFAULT NULL,
  authorized datetime DEFAULT NULL,
  httpMethod varchar(255) DEFAULT NULL,
  requestToken varchar(255) DEFAULT NULL,
  tokenSecret varchar(255) DEFAULT NULL,
  consumer_id bigint(20) DEFAULT NULL,
  user_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FKC0E69FE47140EFE (user_id),
  KEY FKC0E69FEBCBAF92D (consumer_id),
  CONSTRAINT FKC0E69FEBCBAF92D FOREIGN KEY (consumer_id) REFERENCES OAuthConsumer (id),
  CONSTRAINT FKC0E69FE47140EFE FOREIGN KEY (user_id) REFERENCES app_user (id)
);

# --- !Downs

DROP TABLE ID_SEQ;
