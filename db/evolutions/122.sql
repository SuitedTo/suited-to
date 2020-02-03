# --- !Ups
ALTER TABLE Question
  ADD COLUMN totalRating INT(11) DEFAULT NULL,
  ADD COLUMN totalInterviews INT(11) DEFAULT NULL,
  DROP KEY metrics_id,
  DROP FOREIGN KEY Question_ibfk_1,
  DROP COLUMN metrics_id;

UPDATE Question q
  JOIN QuestionMetrics qm on qm.question_id = q.id
  SET q.totalRating = qm.totalRating, q.totalInterviews = qm.totalInterviews;


DROP TABLE QuestionMetrics;

# --- !Downs
ALTER TABLE Question
  DROP COLUMN totalRating,
  DROP COLUMN totalInterviews,
  ADD COLUMN metrics_id bigint(20);

CREATE TABLE QuestionMetrics (
  id bigint(20) NOT NULL,
  question_id bigint(20) DEFAULT NULL,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  totalRating int(11) DEFAULT NULL,
  totalInterviews int(11) DEFAULT NULL,
  PRIMARY KEY (id)
)


