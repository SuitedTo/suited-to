# --- !Ups

ALTER TABLE Question ADD COLUMN flaggedReason varchar(4000) DEFAULT null;

# --- !Downs

ALTER TABLE Question DROP COLUMN flaggedReason;
