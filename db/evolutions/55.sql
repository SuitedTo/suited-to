
# --- !Ups

CREATE TABLE ScheduledJob (
  id bigint(20) NOT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  callString varchar(500) DEFAULT NULL,
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE ScheduledJob;
