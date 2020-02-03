# --- !Ups

ALTER TABLE app_user DROP COLUMN timeZoneOffset;

ALTER TABLE app_user ADD COLUMN timeZone varchar(255) DEFAULT NULL;

UPDATE app_user set timeZone = 'America/New_York';


# --- !Downs

ALTER TABLE app_user ADD COLUMN timeZoneOffset int(3) DEFAULT null;

UPDATE app_user set timeZoneOffset = -300;

ALTER TABLE app_user DROP COLUMN timeZone;
