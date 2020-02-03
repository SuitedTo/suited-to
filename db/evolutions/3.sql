# --- !Ups

CREATE TABLE GateKeeper (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  restriction varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE GateKeeper;