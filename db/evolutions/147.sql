# --- !Ups


#This field doesn't seem to be used anywhere
ALTER TABLE Company DROP COLUMN trialExpired;

ALTER TABLE Company add column trialExpiration datetime DEFAULT NULL;

update Company set trialExpiration = DATE_ADD(created,INTERVAL 60 DAY) where accountType like "STANDARD" and lastFourCardDigits is null;



# --- !Downs

ALTER TABLE Company DROP COLUMN trialExpiration;