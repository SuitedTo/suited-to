# --- !Ups

ALTER TABLE INTERVIEW_QUESTION CHANGE COLUMN comment comment varchar(750);

# --- !Downs

ALTER TABLE INTERVIEW_QUESTION CHANGE COLUMN comment comment varchar(255);
