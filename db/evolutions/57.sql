# --- !Ups

alter table Candidate add column taleoCandId bigint(20) default null;

# --- !Downs

alter table Candidate drop column taleoCandId;