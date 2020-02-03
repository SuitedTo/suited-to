# --- !Ups


CREATE TABLE QuestionDuplication (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE Question ADD COLUMN duplication_id bigint(20);

ALTER TABLE Question ADD FOREIGN KEY (duplication_id) REFERENCES QuestionDuplication (id);


# --- !Downs


DROP TABLE QuestionDuplication;

ALTER TABLE Question DROP COLUMN duplication_id;
