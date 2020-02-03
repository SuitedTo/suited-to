
# --- !Ups

ALTER TABLE Feedback ADD COLUMN activeInterview_id bigint(20) DEFAULT NULL;
ALTER TABLE Workflow ADD COLUMN event_id bigint(20) NOT NULL;
ALTER TABLE TemporaryFeedbackAuthorization 
        ADD COLUMN activeInterview_id bigint(20) DEFAULT NULL;
ALTER TABLE ActiveInterview 
        ADD COLUMN feedbackReminderSent BIT(1) DEFAULT 0;

# --- !Downs

ALTER TABLE Feedback DROP COLUMN activeInterview_id;
ALTER TABLE Workflow DROP COLUMN event_id;
ALTER TABLE TemporaryFeedbackAuthorization DROP COLUMN activeInterview_id;
ALTER TABLE ActiveInterview DROP COLUMN feedbackReminderSent;
