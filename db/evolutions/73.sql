# --- !Ups
ALTER TABLE Workflow ADD COLUMN pass bit(1) DEFAULT NULL;


# --- !Downs
ALTER TABLE Workflow DROP COLUMN;
