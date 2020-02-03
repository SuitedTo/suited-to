
# --- !Ups

ALTER TABLE Category ADD COLUMN companyname varchar(255) DEFAULT null;

# --- !Downs
ALTER TABLE Category DROP COLUMN companyname;
