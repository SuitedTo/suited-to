# --- !Ups
ALTER TABLE PREP_User ADD COLUMN profilePictureUrl varchar(255) DEFAULT null;

# --- !Downs
ALTER TABLE PREP_User DROP COLUMN profilePictureUrl;