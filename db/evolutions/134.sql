# --- !Ups

DELETE FROM PREP_InterviewBuild WHERE interview_id IN (SELECT id FROM PREP_Interview WHERE valid = 0);
DELETE FROM PREP_Interview WHERE valid = 0;

# --- !Downs
