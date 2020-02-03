
# --- !Ups

alter table PREP_User drop column last_charge;
alter table PREP_User add column is_subscriber bit(1) not null;

# --- !Downs

alter table PREP_User add column last_charge datetime default null;
alter table PREP_User drop column is_subscriber;
