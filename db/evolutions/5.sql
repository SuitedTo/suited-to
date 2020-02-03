# --- !Ups

CREATE TABLE Notification (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  user_id bigint(20) DEFAULT NULL,
  workflow_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK2D45DD0B47140EFE (user_id),
  KEY FK2D45DD0B1709DE84 (workflow_id),
  CONSTRAINT FK2D45DD0B1709DE84 FOREIGN KEY (workflow_id) REFERENCES QuestionWorkflow (id),
  CONSTRAINT FK2D45DD0B47140EFE FOREIGN KEY (user_id) REFERENCES app_user (id)
);


# --- !Downs

DROP TABLE Notification;