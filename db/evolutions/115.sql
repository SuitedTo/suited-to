# --- !Ups
ALTER TABLE PREP_User
  ADD COLUMN externalAuthProviderId varchar(255) DEFAULT null,
  ADD COLUMN externalAuthProvider varchar(255) DEFAULT null,
  ADD COLUMN firstName varchar(255) DEFAULT null,
  ADD COLUMN salt varchar(255) DEFAULT NULL,
  ADD COLUMN password varchar(255) DEFAULT NULL,
  ADD COLUMN lastLogin datetime DEFAULT NULL,
  DROP COLUMN name,
  DROP COLUMN userId;

CREATE TABLE PrepUser_roles (
  PrepUser_id bigint(20) NOT NULL,
  roles varchar(255) DEFAULT NULL,
  KEY FKE2E0CF761BE60D17 (PrepUser_id),
  CONSTRAINT FKE2E0CF761BE60D17 FOREIGN KEY (PrepUser_id) REFERENCES PREP_User (id)
);

ALTER TABLE app_user DROP COLUMN facebookUserId;
ALTER TABLE app_user DROP COLUMN linkedInUserId;
ALTER TABLE app_user DROP COLUMN googleUserId;


# --- !Downs
ALTER TABLE PREP_User
  DROP COLUMN externalAuthProviderId,
  DROP COLUMN externalAuthProvider,
  DROP COLUMN firstName,
  DROP COLUMN salt,
  DROP COLUMN password,
  DROP COLUMN lastLogin,
  ADD COLUMN name varchar(255) DEFAULT null,
  ADD COLUMN userId varchar(255) DEFAULT null;

DROP TABLE PrepUser_roles;

ALTER TABLE app_user ADD COLUMN facebookUserId varchar(255) DEFAULT null;
ALTER TABLE app_user ADD COLUMN linkedInUserId varchar(255) DEFAULT null;
ALTER TABLE app_user ADD COLUMN googleUserId varchar(255) DEFAULT null;
