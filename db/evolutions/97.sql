# --- !Ups

CREATE TABLE UserBadgeMetrics (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  usersEarnedCount int(11) DEFAULT 0,
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE UserBadgeMetrics;