# --- !Ups
ALTER TABLE app_user ADD COLUMN googleOpenIdUrl varchar(1000) DEFAULT NULL;
ALTER TABLE app_user ADD COLUMN googleOpenIdEmail varchar(255) DEFAULT NULL;

# --- !Downs
ALTER TABLE app_user DROP COLUMN googleOpenIdUrl;
ALTER TABLE app_user DROP COLUMN googleOpenIdEmail;