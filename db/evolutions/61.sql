# --- !Ups

ALTER TABLE Company ADD COLUMN trialExpired bit(1) NOT NULL DEFAULT 0;




# --- !Downs


ALTER TABLE Company DROP COLUMN trialExpired;