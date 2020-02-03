# --- !Ups

alter table PREP_Question modify staticAnswers longtext;

# --- !Downs

alter table PREP_Question modify staticAnswers varchar(255);