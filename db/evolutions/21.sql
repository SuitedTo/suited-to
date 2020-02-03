
# --- !Ups

ALTER TABLE Feedback DROP COLUMN sourceemail;

ALTER TABLE Feedback ADD COLUMN feedbackEmail varchar(255) DEFAULT null;

# --- !Downs

ALTER TABLE Feedback ADD COLUMN sourceemail varchar(255) DEFAULT null;

ALTER TABLE Feedback DROP COLUMN feedbackEmail;
