# --- !Ups

ALTER TABLE CategoryOverride MODIFY COLUMN reviewerAllowed bit(1) NULL;
ALTER TABLE CategoryOverride MODIFY COLUMN proInterviewerAllowed bit(1) NULL;

# --- !Downs

ALTER TABLE CategoryOverride MODIFY COLUMN reviewerAllowed bit(1) NOT NULL;
ALTER TABLE CategoryOverride MODIFY COLUMN proInterviewerAllowed bit(1) NOT NULL;