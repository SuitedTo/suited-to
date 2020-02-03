# --- !Ups

alter table PREP_Question modify tips longtext;

alter table PREP_Question modify text varchar(1000);


# --- !Downs

alter table PREP_Question modify tips varchar(255);

alter table PREP_Question modify text varchar(255);