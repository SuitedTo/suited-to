# --- !Ups

ALTER TABLE PREP_InterviewCategoryList ADD COLUMN name VARCHAR(255) DEFAULT NULL;

# --- !Downs

ALTER TABLE PREP_InterviewCategoryList DROP COLUMN name;