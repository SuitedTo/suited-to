# --- !Ups
ALTER TABLE Category DROP COLUMN isPrepSearchable;
ALTER TABLE Category DROP COLUMN isPrep;

# --- !Downs
ALTER TABLE Category ADD COLUMN isPrep bit(1) DEFAULT b'0';
UPDATE Category SET isPrep = 0;
ALTER TABLE Category ADD COLUMN isPrepSearchable bit(1) DEFAULT b'0';
UPDATE Category SET isPrepSearchable = 0;