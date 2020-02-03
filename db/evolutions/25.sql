
# --- !Ups

ALTER TABLE Company ADD COLUMN accountTypeLock bit(1) DEFAULT 0;

# --- !Downs

ALTER TABLE Company DROP COLUMN accountTypeLock;