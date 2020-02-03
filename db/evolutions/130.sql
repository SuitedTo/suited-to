# --- !Ups

CREATE TABLE PREP_Coupon
(
    id BIGINT PRIMARY KEY NOT NULL,
    created DATETIME,
    updated DATETIME,
    currentUses INT,
    discount INT,
    expirationDate DATETIME,
    maxUses INT,
    name VARCHAR(255),
    UNIQUE (name)
);

ALTER TABLE PREP_User ADD COLUMN coupon_id bigint(20);
ALTER TABLE PREP_User ADD CONSTRAINT prepuser_prepcoupon FOREIGN KEY ( coupon_id ) REFERENCES PREP_Coupon ( id );

# --- !Downs

ALTER TABLE PREP_User DROP FOREIGN KEY prepuser_prepcoupon;
ALTER TABLE PREP_User DROP COLUMN coupon_id;

DROP TABLE PREP_Coupon;