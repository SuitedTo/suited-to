# --- !Ups

ALTER TABLE Question ALTER COLUMN interviewPoints SET DEFAULT 0;
UPDATE Question set interviewPoints=0 where interviewPoints is null;

ALTER TABLE Question ALTER COLUMN totalRating SET DEFAULT 0;
UPDATE Question set totalRating=0 where totalRating is null;

ALTER TABLE Question ALTER COLUMN totalInterviews SET DEFAULT 0;
UPDATE Question set totalInterviews=0 where totalInterviews is null;


# --- !Downs


ALTER TABLE Question ALTER COLUMN interviewPoints SET DEFAULT NULL;
ALTER TABLE Question ALTER COLUMN totalRating SET DEFAULT NULL;
ALTER TABLE Question ALTER COLUMN totalInterviews SET DEFAULT NULL;