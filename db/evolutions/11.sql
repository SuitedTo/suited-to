
# --- !Ups

ALTER TABLE Question ADD COLUMN active bit(1) NOT NULL DEFAULT 1;

# --- !Downs

ALTER TABLE Question DROP COLUMN active;
