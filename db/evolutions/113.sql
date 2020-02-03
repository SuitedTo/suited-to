# --- !Ups
ALTER TABLE app_user ADD COLUMN googleUserId varchar(255) DEFAULT null;

# --- !Downs
ALTER TABLE app_user DROP COLUMN googleUserId;