
# --- !Ups

ALTER TABLE question_metadata
ADD COLUMN created datetime DEFAULT NULL,
ADD COLUMN updated datetime DEFAULT NULL,
ADD COLUMN lastActivity datetime DEFAULT NULL;


# --- !Downs

ALTER TABLE question_metadata DROP COLUMN created, DROP COLUMN updated, DROP COLUMN lastActivity;
