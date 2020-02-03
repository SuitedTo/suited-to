
# --- !Ups

ALTER TABLE Interview ADD COLUMN active bit(1) DEFAULT 1;

# --- !Downs

ALTER TABLE Interview DROP COLUMN active;