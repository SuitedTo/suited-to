# --- !Ups

ALTER TABLE Company ADD COLUMN delinquent bit(1) NOT NULL DEFAULT 0;




# --- !Downs


ALTER TABLE Company DROP COLUMN delinquent;
