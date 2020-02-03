
# --- !Ups

ALTER TABLE Company ADD COLUMN taleoIntegration bit(1) NOT NULL DEFAULT 0;

# --- !Downs

ALTER TABLE Company DROP COLUMN taleoIntegration;
