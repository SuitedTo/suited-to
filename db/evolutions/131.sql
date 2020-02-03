# --- !Ups
CREATE TABLE PrepUser_currentCouponChargeHistory
(
    PrepUser_id BIGINT NOT NULL,
    currentCouponChargeHistory DATETIME,
    FOREIGN KEY ( PrepUser_id ) REFERENCES PREP_User ( id )
);

ALTER TABLE PREP_Coupon ADD COLUMN payPeriods INT;
ALTER TABLE PREP_Coupon DROP COLUMN expirationDate;

# --- !Downs

ALTER TABLE PREP_Coupon ADD COLUMN expirationDate DATETIME;
ALTER TABLE PREP_Coupon DROP COLUMN payPeriods;

DROP TABLE PrepUser_currentCouponChargeHistory;