
# --- !Ups

ALTER TABLE CandidateFile ADD COLUMN creator_id bigint(20) default null;
ALTER TABLE CandidateFile MODIFY candidate_id bigint(20) default null;


# --- !Downs
ALTER TABLE CandidateFile DROP COLUMN creator_id;