# --- !Ups

ALTER TABLE PREP_Interview ADD COLUMN valid bit(1) DEFAULT 1;

# --- !Downs

ALTER TABLE PREP_Interview DROP COLUMN valid;
