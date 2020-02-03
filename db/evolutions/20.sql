
# --- !Ups

CREATE TABLE TemporaryFeedbackAuthorization (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    created datetime DEFAULT NULL,
    updated datetime DEFAULT NULL,
    candidate_id bigint(20) DEFAULT NULL,
    email varchar(255) DEFAULT NULL,
    nonce int(11) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY candidateKey (candidate_id),
    CONSTRAINT candidateKey FOREIGN KEY (candidate_id) REFERENCES Candidate(id)
); 

ALTER TABLE Feedback ADD COLUMN sourceemail varchar(255) DEFAULT null;

# --- !Downs

ALTER TABLE Feedback DROP COLUMN sourceemail;

DROP TABLE TemporaryFeedbackAuthorization;