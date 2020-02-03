# --- !Ups

ALTER TABLE Company
ADD COLUMN feedbackDisplay BIT(1) DEFAULT 1;

# --- !Downs

ALTER TABLE Company DROP COLUMN feedbackDisplay;