# --- !Ups

ALTER TABLE PREP_Question ADD COLUMN category_name varchar(255) DEFAULT NULL;

# --- !Downs

ALTER TABLE PREP_Question DROP COLUMN category_name;