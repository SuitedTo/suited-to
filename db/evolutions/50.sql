# --- !Ups

ALTER TABLE Question DROP COLUMN privateQuestion;


# --- !Downs


ALTER TABLE Question ADD COLUMN privateQuestion bit(1) NOT NULL;
