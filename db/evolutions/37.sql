
# --- !Ups

ALTER TABLE Question ADD COLUMN flaggedAsInappropriate bit(1) NOT NULL DEFAULT 0;

ALTER TABLE app_user ADD COLUMN streetCred bigint(20);


 # --- !Downs

 ALTER TABLE Question DROP COLUMN flaggedAsInappropriate;

 ALTER TABLE app_user DROP COLUMN streetCred;
