
# --- !Ups

ALTER TABLE app_user ADD COLUMN thumbnail varchar(255) DEFAULT null;


# --- !Downs


ALTER TABLE app_user DROP COLUMN thumbnail;
