
# --- !Ups

alter table PREP_User
  add column apple_receipt longtext,
  add column payment_verified bit(1) not null default 0;

update PREP_User
  set payment_verified = 1 where stripe_id is not null;

update PREP_User
  set payment_verified = 0 where stripe_id is null;


# --- !Downs

alter table PREP_User
  drop column apple_receipt,
  drop column payment_verified;

