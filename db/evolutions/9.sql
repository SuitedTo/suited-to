# --- !Ups

CREATE TABLE QUESTION_NOTE (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  text longtext,
  company_id bigint(20) DEFAULT NULL,
  question_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK7E14B4AB1366CCF6 (company_id),
  KEY FK7E14B4AB8F77231E (question_id),
  CONSTRAINT FK7E14B4AB8F77231E FOREIGN KEY (question_id) REFERENCES Question (id),
  CONSTRAINT FK7E14B4AB1366CCF6 FOREIGN KEY (company_id) REFERENCES Company (id)
);

# --- !Downs

DROP TABLE QUESTION_NOTE;