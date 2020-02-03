
# --- !Ups

ALTER TABLE ActiveInterview DROP FOREIGN KEY interviewKey;

ALTER TABLE ActiveInterview ADD CONSTRAINT activeInterviewIntervieweeIsCandidate
        FOREIGN KEY (interviewee_id) REFERENCES Candidate(id);

ALTER TABLE ActiveInterview ADD CONSTRAINT activeInterviewInterviewIsInterview
        FOREIGN KEY (id) REFERENCES Interview(id);

# --- !Downs

ALTER TABLE ActiveInterview 
        DROP FOREIGN KEY activeInterviewIntervieweeIsCandidate;

ALTER TABLE ActiveInterview 
        DROP FOREIGN KEY activeInterviewInterviewIsInterview;

ALTER TABLE ActiveInterview ADD CONSTRAINT interviewKey 
        FOREIGN KEY (interviewee_id) REFERENCES Interview(id);
