
# --- !Ups

CREATE TABLE ActiveInterview (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    anticipatedDate datetime DEFAULT NULL,
    interviewee_id bigint(20) NOT NULL,
    PRIMARY KEY (id),
    KEY interviewKey (interviewee_id),
    CONSTRAINT interviewKey FOREIGN KEY (interviewee_id) REFERENCES Interview(id)
);

INSERT INTO GateKeeper (restriction) VALUES ('ActiveInterview');


# --- !Downs

DROP TABLE ActiveInterview;

DELETE FROM GateKeeper WHERE restriction='ActiveInterview';
