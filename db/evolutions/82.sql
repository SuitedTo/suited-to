
# --- !Ups

ALTER TABLE Story ADD COLUMN metadata longtext DEFAULT NULL;


# --- !Downs

ALTER TABLE Story DROP COLUMN metadata;