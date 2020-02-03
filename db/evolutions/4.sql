# --- !Ups

ALTER TABLE QuestionWorkflow DROP FOREIGN KEY FKE74D75E5ADB01AF0;

DROP TABLE QuestionComment;

ALTER TABLE QuestionWorkflow DROP COLUMN comment_id;

ALTER TABLE QuestionWorkflow ADD COLUMN comment LONGTEXT;

# --- !Downs

