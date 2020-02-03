# --- !Ups

ALTER TABLE ActiveInterview ADD COLUMN notes longtext;


# --- !Downs


ALTER TABLE ActiveInterview DROP COLUMN notes;
