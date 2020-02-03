# --- !Ups

ALTER TABLE Company
ADD COLUMN previousAccountType varchar(255) DEFAULT NULL,
ADD COLUMN lastAccountChangeDate datetime DEFAULT NULL;

# --- !Downs

ALTER TABLE Company DROP COLUMN previousAccountType, DROP COLUMN lastAccountChangeDate;
