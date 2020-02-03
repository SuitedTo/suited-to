
# --- !Ups

ALTER TABLE TemporaryFeedbackAuthorization ADD COLUMN candidateInterview_id bigint(20) DEFAULT NULL;

ALTER TABLE app_user ADD COLUMN feedbackReplyEmails bit(1) DEFAULT null;

UPDATE app_user set feedbackReplyEmails = 1;

# --- !Downs

ALTER TABLE app_user DROP COLUMN feedbackReplyEmails;
