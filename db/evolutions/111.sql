# --- !Ups
ALTER TABLE app_user ADD COLUMN facebookUserId varchar(255) DEFAULT null;

# --- !Downs
ALTER TABLE app_user DROP COLUMN facebookUserId;