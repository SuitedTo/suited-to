# --- !Ups

CREATE TABLE DataTrigger (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  triggerKey varchar(255) DEFAULT NULL,
  propertyName varchar(255) DEFAULT NULL,
  type varchar(255) DEFAULT NULL,
  domain varchar(255) DEFAULT NULL,
  subdomain varchar(255) DEFAULT NULL,
  targetValue varchar(255) DEFAULT NULL,
  active bit(1) NOT NULL,
  UNIQUE KEY key_domain (triggerKey,domain),
  PRIMARY KEY (id)
);

CREATE TABLE UserMetrics (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  user_id bigint(20) DEFAULT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  reviewer bit(1) NOT NULL,
  numberOfAcceptedQuestions int(11) DEFAULT NULL,
  totalQuestionScore int(11) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK_USER_USERMETRICS (user_id),
  CONSTRAINT FK_USER_USERMETRICS FOREIGN KEY (user_id) REFERENCES app_user (id)
);

ALTER TABLE app_user ADD COLUMN metrics_id bigint(20);

ALTER TABLE app_user ADD FOREIGN KEY (metrics_id) REFERENCES UserMetrics (id);

# --- !Downs


SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE app_user DROP COLUMN metrics_id;

DROP TABLE UserMetrics;

DROP TABLE JobTrigger;

SET FOREIGN_KEY_CHECKS = 1;
