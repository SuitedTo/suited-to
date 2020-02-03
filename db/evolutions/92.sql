# --- !Ups

CREATE TABLE CategoryOverride (
  id bigint(20) NOT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  proInterviewerAllowed bit(1) NOT NULL,
  reviewerAllowed bit(1) NOT NULL,
  category_id bigint(20) DEFAULT NULL,
  user_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FKD054AB2F47140EFE (user_id),
  KEY FKD054AB2FFA266C1E (category_id),
  CONSTRAINT FKD054AB2FFA266C1E FOREIGN KEY (category_id) REFERENCES Category (id),
  CONSTRAINT FKD054AB2F47140EFE FOREIGN KEY (user_id) REFERENCES app_user (id)
);

# --- !Downs

DROP TABLE CategoryOverride;
