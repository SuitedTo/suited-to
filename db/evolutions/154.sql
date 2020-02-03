# --- !Ups


CREATE TABLE PREP_AnswerVideo (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  contents varchar(255) NOT NULL,
  name varchar(255) DEFAULT NULL,
  type varchar(255) DEFAULT NULL,
  owner_id bigint(20) NOT NULL,
  question_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  KEY FKDAD7D02B344E3D04 (question_id),
  KEY FKDAD7D02BA986BBFC (owner_id),
  CONSTRAINT FKDAD7D02BA986BBFC FOREIGN KEY (owner_id) REFERENCES PREP_User (id),
  CONSTRAINT FKDAD7D02B344E3D04 FOREIGN KEY (question_id) REFERENCES PREP_Question (id)
);

# --- !Downs

DROP TABLE PREP_AnswerVideo;