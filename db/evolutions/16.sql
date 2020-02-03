
# --- !Ups

ALTER TABLE app_user ADD COLUMN picture varchar(255) DEFAULT null;

ALTER TABLE app_user ADD COLUMN publicEmail bit(1) DEFAULT null;

UPDATE app_user set publicEmail = 1;



# --- !Downs


ALTER TABLE app_user DROP COLUMN picture;

ALTER TABLE app_user DROP COLUMN publicEmail;
