# --- !Ups

ALTER TABLE Company ADD COLUMN lastFourCardDigits varchar(255) DEFAULT null;


# --- !Downs


ALTER TABLE Company DROP COLUMN lastFourCardDigits;
