# --- !Ups


CREATE TABLE CandidateFile (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  contents varchar(255) DEFAULT null,
  name varchar(255) DEFAULT null,
  type varchar(255) DEFAULT null,
  candidate_id bigint(20) NOT NULL,
  KEY FK_CANDIDATEFILE_CANDIDATE (candidate_id),
  CONSTRAINT FK_CANDIDATEFILE_CANDIDATE FOREIGN KEY (candidate_id) REFERENCES Candidate (id),
  PRIMARY KEY (id)
);


# --- !Downs


DROP TABLE CandidateFile;
