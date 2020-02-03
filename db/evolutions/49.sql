
# --- !Ups


CREATE TABLE UserBadge (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  user_id bigint(20) DEFAULT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  multiplier int(11) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK_USER_USERBADGE (user_id),
  CONSTRAINT FK_USER_USERBADGE FOREIGN KEY (user_id) REFERENCES app_user (id)
);


# --- !Downs

DROP TABLE UserBadge;
