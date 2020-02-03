
# --- !Ups

ALTER TABLE Company ADD COLUMN status varchar(255) DEFAULT 'ACTIVE';

# --- !Downs

ALTER TABLE Company DROP COLUMN status;