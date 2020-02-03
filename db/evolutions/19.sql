
# --- !Ups
ALTER TABLE app_user ADD COLUMN lastLogin datetime DEFAULT NULL;
ALTER TABLE app_user ADD COLUMN tempPassword varchar(255) DEFAULT NULL;


# --- !Downs
ALTER TABLE app_user DROP COLUMN lastLogin;
ALTER TABLE app_user DROP COLUMN tempPassword;