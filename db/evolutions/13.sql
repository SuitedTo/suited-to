# --- !Ups

ALTER TABLE Interview DROP COLUMN time;


# --- !Downs

ALTER TABLE Interview ADD COLUMN time int(11) DEFAULT NULL;
