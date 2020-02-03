# --- !Ups

CREATE TABLE TriggerEntry (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  triggerKey varchar(255) NOT NULL,
  taskClass varchar(255) NOT NULL,
  taskArgs text DEFAULT NULL,
  scheduled bit(1) DEFAULT 0,
  PRIMARY KEY (id)
);


CREATE TABLE CronTrigger (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  cronExpression varchar(255) NOT NULL,
  timeZone varchar(255) DEFAULT NULL,
  reschedule bit(1) DEFAULT 1,
  allTimeZones bit(1) DEFAULT 0,
  triggerentry_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  KEY FK_CRONTRIGGER_TRIGGERENTRY (triggerentry_id),
  CONSTRAINT FK_CRONTRIGGER_TRIGGERENTRY FOREIGN KEY (triggerentry_id) REFERENCES TriggerEntry (id)
);
