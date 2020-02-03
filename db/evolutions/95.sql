# --- !Ups
ALTER TABLE ActiveInterviewStateChange ADD COLUMN creationTimestamp bigint(20) DEFAULT NULL;

# --- !Downs
ALTER TABLE ActiveInterviewStateChange DROP COLUMN creationTimestamp;