# --- !Ups
CREATE TABLE Event (
  id bigint(20) NOT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  eventType varchar(255) DEFAULT NULL,
  metadata longtext DEFAULT NULL,
  relatedUser_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK403827A635F7C73 (relatedUser_id),
  CONSTRAINT FK403827A635F7C73 FOREIGN KEY (relatedUser_id) REFERENCES app_user (id)
);

CREATE TABLE Story (
  id bigint(20) NOT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  affinity double DEFAULT NULL,
  allUsers bit(1) NOT NULL,
  creationTimeInMillis bigint(20) DEFAULT NULL,
  weight double DEFAULT NULL,
  company_id bigint(20) DEFAULT NULL,
  event_id bigint(20) DEFAULT NULL,
  user_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK4C808D51366CCF6 (company_id),
  KEY FK4C808D5BAFEA2D6 (event_id),
  KEY FK4C808D547140EFE (user_id),
  CONSTRAINT FK4C808D547140EFE FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT FK4C808D51366CCF6 FOREIGN KEY (company_id) REFERENCES Company (id),
  CONSTRAINT FK4C808D5BAFEA2D6 FOREIGN KEY (event_id) REFERENCES Event (id)
);

# --- !Downs

DROP TABLE Story;
DROP TABLE Event;