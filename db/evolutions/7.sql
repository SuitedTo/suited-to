# --- !Ups

drop table app_user_Category;
CREATE TABLE app_user_category (
  app_user_id bigint(20) NOT NULL,
  category_id bigint(20) NOT NULL,
  UNIQUE KEY user_category (app_user_id, category_id),
  KEY FK6B5AF97447140EFE (app_user_id),
  KEY FK6B5AF974DFEE9DE8 (category_id),
  CONSTRAINT FK6B5AF974DFEE9DE8 FOREIGN KEY (category_id) REFERENCES Category (id),
  CONSTRAINT FK6B5AF97447140EFE FOREIGN KEY (app_user_id) REFERENCES app_user (id)
);