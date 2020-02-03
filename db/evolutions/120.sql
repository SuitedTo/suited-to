# --- !Ups

alter table PREP_Question modify answers longtext;


# --- !Downs

alter table PREP_Question modify answers varchar(255);