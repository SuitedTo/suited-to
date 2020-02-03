# --- !Ups

ALTER TABLE Company ADD COLUMN deactivationdate datetime DEFAULT null;

# --- !Downs

ALTER TABLE Company DROP COLUMN deactivationdate;