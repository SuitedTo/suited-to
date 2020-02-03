# --- !Ups

ALTER TABLE CandidateFile ADD COLUMN docType varchar(255) NOT NULL DEFAULT "OTHER";




# --- !Downs


ALTER TABLE CandidateFile DROP COLUMN docType;


