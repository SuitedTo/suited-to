# --- !Ups

ALTER TABLE Category ADD COLUMN exportedToPrep bit(1) DEFAULT b'0';

# --- !Downs

ALTER TABLE Category DROP COLUMN exportedToPrep;
