# --- !Ups

CREATE TABLE QuestionMetrics (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  question_id bigint(20) DEFAULT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  totalRating int(11) DEFAULT NULL,
  totalInterviews int(11) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK_QUESTION_QUESTIONMETRICS (question_id),
  CONSTRAINT FK_QUESTION_QUESTIONMETRICS FOREIGN KEY (question_id) REFERENCES Question (id)
);

ALTER TABLE Question ADD COLUMN metrics_id bigint(20);

ALTER TABLE Question ADD COLUMN standardScore int(11) default 0;

ALTER TABLE question_metadata ADD COLUMN interviewScore int(11) default 0;

ALTER TABLE Question ADD FOREIGN KEY (metrics_id) REFERENCES QuestionMetrics (id);

# --- !Downs

ALTER TABLE question_metadata DROP COLUMN interviewScore;

ALTER TABLE Question DROP COLUMN standardScore;

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE Question DROP COLUMN metrics_id;

DROP TABLE QuestionMetrics;

SET FOREIGN_KEY_CHECKS = 1;
