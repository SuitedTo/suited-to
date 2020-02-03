# --- !Ups

ALTER TABLE ActiveInterview ADD COLUMN averageQuestionRating double DEFAULT NULL;

# --- !Downs

ALTER TABLE ActiveInterview DROP COLUMN averageQuestionRating;