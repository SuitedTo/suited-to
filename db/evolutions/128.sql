# --- !Ups

ALTER TABLE PREP_User ADD COLUMN lastFourCardDigits VARCHAR(255) DEFAULT NULL;

# --- !Downs

ALTER TABLE PREP_User DROP COLUMN lastFourCardDigits;
