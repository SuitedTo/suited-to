# --- !Ups

ALTER TABLE Feedback DROP FOREIGN KEY FKF8704FA54B0E52FE;

ALTER TABLE Feedback DROP COLUMN candidateInterview_id;
ALTER TABLE TemporaryFeedbackAuthorization DROP COLUMN candidateInterview_id;

DROP TABLE CandidateInterview;

# --- !Downs

CREATE TABLE CandidateInterview (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  date datetime DEFAULT NULL,
  candidate_id bigint(20) DEFAULT NULL,
  interview_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK306D9B5EA1A3D776 (interview_id),
  KEY FK306D9B5E2A3E4036 (candidate_id),
  CONSTRAINT FK306D9B5E2A3E4036 FOREIGN KEY (candidate_id) REFERENCES Candidate (id),
  CONSTRAINT FK306D9B5EA1A3D776 FOREIGN KEY (interview_id) REFERENCES Interview (id)
);

ALTER TABLE Feedback ADD COLUMN candidateInterview_id bigint(20) DEFAULT NULL;
ALTER TABLE Feedback ADD CONSTRAINT FKF8704FA54B0E52FE 
    FOREIGN KEY (candidateInterview_id) REFERENCES CandidateInterview (id);

ALTER TABLE TemporaryFeedbackAuthorization ADD COLUMN candidateInterview_id bigint(20) DEFAULT NULL;
