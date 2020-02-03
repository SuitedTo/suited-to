# --- !Ups

ALTER TABLE Question ADD COLUMN interviewPoints INT(11) DEFAULT NULL;
ALTER TABLE question_metadata DROP COLUMN interviewScore;
ALTER TABLE question_metadata ADD COLUMN ratingPoints INT(11) DEFAULT NULL;

# --- !Downs

ALTER TABLE Question DROP COLUMN interviewPoints;
ALTER TABLE question_metadata ADD COLUMN interviewScore INT(11) DEFAULT NULL;
ALTER TABLE question_metadata DROP COLUMN ratingPoints;