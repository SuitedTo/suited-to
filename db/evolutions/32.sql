
# --- !Ups

ALTER TABLE Company DROP COLUMN privateQuestionEnabled;


# --- !Downs

ALTER TABLE Company ADD COLUMN privateQuestionEnabled bit(1) default 0;
