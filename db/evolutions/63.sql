# --- !Ups

ALTER TABLE app_user ADD COLUMN interviewStopReminder bit(1) NOT NULL DEFAULT 0;
ALTER TABLE app_user ADD COLUMN feedbackRequestedReminder bit(1) NOT NULL DEFAULT 0;




# --- !Downs

ALTER TABLE app_user DROP COLUMN interviewStopReminder;
ALTER TABLE app_user DROP COLUMN feedbackRequestReminder;
