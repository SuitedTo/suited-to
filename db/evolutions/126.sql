# --- !Ups

ALTER TABLE PREP_User ADD COLUMN lastCharge DATETIME DEFAULT NULL;
ALTER TABLE PREP_User ADD COLUMN stripeId VARCHAR(255) DEFAULT NULL;


# --- !Downs

ALTER TABLE PREP_User DROP COLUMN stripeId;
ALTER TABLE PREP_User DROP COLUMN lastCharge;