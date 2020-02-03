
# --- !Ups

ALTER TABLE PREP_Category ADD COLUMN name varchar(255) DEFAULT null;

ALTER TABLE PREP_Category ADD COLUMN companyName varchar(255) DEFAULT null;

ALTER TABLE PREP_Question ADD COLUMN text varchar(255) DEFAULT null;

ALTER TABLE PREP_Question ADD COLUMN answers varchar(255) DEFAULT null;

ALTER TABLE PREP_Question ADD COLUMN tips varchar(255) DEFAULT null;


# --- !Downs


ALTER TABLE PREP_Category DROP COLUMN name;

ALTER TABLE PREP_Category DROP COLUMN companyName;

ALTER TABLE PREP_Question DROP COLUMN text;

ALTER TABLE PREP_Question DROP COLUMN answers;

ALTER TABLE PREP_Question DROP COLUMN tips;