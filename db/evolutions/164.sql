# --- !Ups

alter table PREP_User drop column last_four_card_digits;
alter table PREP_User drop foreign key prepuser_prepcoupon;
alter table PREP_User drop column coupon_id;



# --- !Downs

alter table PREP_User add column coupon_id bigint(20) DEFAULT NULL;
alter table PREP_User add column last_four_card_digits varchar(255) DEFAULT NULL;
alter table PREP_User add constraint prepuser_prepcoupon foreign key (coupon_id) references coupon(id);
