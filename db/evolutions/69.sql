
# --- !Ups
ALTER TABLE CronTrigger ADD COLUMN previousExecution datetime DEFAULT NULL;

# --- !Downs
ALTER TABLE CronTrigger DROP COLUMN previousExecution;
