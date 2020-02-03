# --- !Ups

ALTER TABLE PREP_Interview
  ADD COLUMN currentQuestion INT(11) DEFAULT 1;


# --- !Downs


ALTER TABLE PREP_Interview
  DROP COLUMN currentQuestion;