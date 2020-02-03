
# --- !Ups

alter table PREP_User drop column is_subscriber;
alter table PREP_User drop column payment_verified;
alter table PREP_User drop column apple_receipt;


# --- !Downs

alter table PREP_User add column is_subscriber bit(1) not null default 0;
alter table PREP_User add column payment_verified bit(1) not null default 0;
alter table PREP_User add column apple_receipt longtext;
