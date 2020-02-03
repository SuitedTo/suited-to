# --- !Ups

DELETE FROM INTERVIEW_QUESTION WHERE
        interview_id IN (SELECT id FROM Interview WHERE active = 0);

# --- !Downs
