
# --- !Ups

CREATE TABLE Feedback (
 id bigint(20) NOT NULL AUTO_INCREMENT,
 created datetime DEFAULT NULL,
 updated datetime DEFAULT NULL,
 adminVisibleOnlyFlag bit(1) NOT NULL,
 comments varchar(255) DEFAULT NULL,
 summaryChoice int(11) DEFAULT NULL,
 candidate_id bigint(20) DEFAULT NULL,
 candidateInterview_id bigint(20) DEFAULT NULL,
 feedbackSource_id bigint(20) DEFAULT NULL,
 PRIMARY KEY (id),
 KEY FKF8704FA54B0E52FE (candidateInterview_id),
 KEY FKF8704FA5A2A7AAC9 (feedbackSource_id),
 KEY FKF8704FA52A3E4036 (candidate_id),
 CONSTRAINT FKF8704FA52A3E4036 FOREIGN KEY (candidate_id) REFERENCES Candidate (id),
 CONSTRAINT FKF8704FA54B0E52FE FOREIGN KEY (candidateInterview_id) REFERENCES CandidateInterview (id),
 CONSTRAINT FKF8704FA5A2A7AAC9 FOREIGN KEY (feedbackSource_id) REFERENCES app_user (id)
);

ALTER TABLE Candidate ADD COLUMN feedbackhidden bit(1) DEFAULT 0;

# --- !Downs

ALTER TABLE Candidate DROP COLUMN feedbackhidden;

DROP TABLE Feedback;