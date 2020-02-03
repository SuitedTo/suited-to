
# --- !Ups

alter table Feedback modify comments longtext;

# --- !Downs

alter table Feedback modify comments VARCHAR(255);