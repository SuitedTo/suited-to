# --- !Ups
ALTER TABLE Workflow MODIFY comment longtext;

# --- !Downs
ALTER TABLE Workflow MODIFY comment varchar(255);