# --- !Ups

ALTER TABLE PrepUser_roles ADD COLUMN id bigint(20) PRIMARY KEY NOT NULL AUTO_INCREMENT;
ALTER TABLE PrepUser_roles ADD COLUMN created datetime DEFAULT NULL;
ALTER TABLE PrepUser_roles ADD COLUMN updated datetime DEFAULT NULL;

ALTER TABLE PrepUser_currentCouponChargeHistory ADD COLUMN id bigint(20) PRIMARY KEY NOT NULL AUTO_INCREMENT;
ALTER TABLE PrepUser_currentCouponChargeHistory ADD COLUMN created datetime DEFAULT NULL;
ALTER TABLE PrepUser_currentCouponChargeHistory ADD COLUMN updated datetime DEFAULT NULL;

# --- !Downs

ALTER TABLE PrepUser_roles DROP COLUMN id;
ALTER TABLE PrepUser_roles DROP COLUMN created;
ALTER TABLE PrepUser_roles DROP COLUMN updated;

ALTER TABLE PrepUser_currentCouponChargeHistory DROP COLUMN id;
ALTER TABLE PrepUser_currentCouponChargeHistory DROP COLUMN created;
ALTER TABLE PrepUser_currentCouponChargeHistory DROP COLUMN updated;