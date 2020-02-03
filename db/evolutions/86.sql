# --- !Ups

CREATE TABLE app_user_pro_interviewer_category (
  app_user_id bigint(20) NOT NULL,
  category_id bigint(20) NOT NULL,
  UNIQUE KEY user_category (app_user_id,category_id),
  KEY FKAN6NZCMFD2VQIY98 (app_user_id),
  KEY FKM7DZVT8GP8HNNQBD (category_id),
  CONSTRAINT FKM7DZVT8GP8HNNQBD FOREIGN KEY (category_id) REFERENCES Category (id),
  CONSTRAINT FKAN6NZCMFD2VQIY98 FOREIGN KEY (app_user_id) REFERENCES app_user (id)
);

# --- !Downs

DROP TABLE app_user_pro_interviewer_category;
