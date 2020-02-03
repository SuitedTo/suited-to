
# --- !Ups

alter table Question add column prepAnswers longtext;
alter table Question add column excludeFromPrep bit(1) not null;

# --- !Downs

alter table Question drop column prepAnswers;
alter table Question drop column excludeFromPrep;
