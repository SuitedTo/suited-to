
# --- !Ups

ALTER TABLE Company ADD COLUMN accountType varchar(255), ADD COLUMN paymentSystemKey varchar(255);


# --- !Downs

ALTER TABLE Company DROP COLUMN accountType, DROP COLUMN paymentSystemKey;
