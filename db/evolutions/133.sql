# --- !Ups

ALTER TABLE PREP_Interview
  ALTER COLUMN currentQuestion SET DEFAULT 0;


# --- !Downs


ALTER TABLE PREP_Interview
  ALTER COLUMN currentQuestion SET DEFAULT 1;