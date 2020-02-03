# --- !Ups

ALTER TABLE app_user ADD COLUMN timeZoneOffset int(3) DEFAULT null;

UPDATE app_user set timeZoneOffset = -300;


# --- !Downs


ALTER TABLE app_user DROP COLUMN timeZoneOffset;
