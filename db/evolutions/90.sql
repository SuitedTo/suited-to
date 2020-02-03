
# --- !Ups

ALTER TABLE Candidate ADD COLUMN user_id bigint(20) DEFAULT NULL;

ALTER TABLE Candidate ADD CONSTRAINT CANDIDATE_USER
        FOREIGN KEY (user_id) REFERENCES app_user(id);

# --- !Downs


ALTER TABLE Candidate DROP FOREIGN KEY CANDIDATE_USER;

ALTER TABLE Candidate DROP COLUMN user_id;


