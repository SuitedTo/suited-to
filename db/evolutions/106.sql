# --- !Ups

CREATE TABLE PREP_JobCategory
(
  id BIGINT PRIMARY KEY NOT NULL,
  created DATETIME,
  updated DATETIME,
  difficulty BIGINT,
  primaryCategory BIT,
  weight BIGINT,
  prepCategory_id BIGINT,
  prepJob_id BIGINT,
  prepJobLevel_id BIGINT
);
CREATE TABLE PREP_JobName
(
  id BIGINT PRIMARY KEY NOT NULL,
  created DATETIME,
  updated DATETIME,
  name VARCHAR(255),
  prepJob_id BIGINT
);
CREATE TABLE PREP_JobLevel
(
  id BIGINT PRIMARY KEY NOT NULL,
  created DATETIME,
  updated DATETIME,
  name VARCHAR(255)
);
CREATE TABLE PREP_Job
(
  id BIGINT PRIMARY KEY NOT NULL,
  created DATETIME,
  updated DATETIME,
  primaryName_id BIGINT
);
ALTER TABLE PREP_JobCategory ADD CONSTRAINT fk_prepJobLevel_id_PREP_JobCategory FOREIGN KEY ( prepJobLevel_id ) REFERENCES PREP_JobLevel ( id );
ALTER TABLE PREP_JobCategory ADD CONSTRAINT fk_prepJob_id_PREP_JobCategory FOREIGN KEY ( prepJob_id ) REFERENCES PREP_Job ( id );
ALTER TABLE PREP_JobCategory ADD CONSTRAINT fk_prepCategory_id_PREP_JobCategory FOREIGN KEY ( prepCategory_id ) REFERENCES PREP_Category ( id );
ALTER TABLE PREP_JobName ADD CONSTRAINT fk_prepJob_id_PREP_JobName FOREIGN KEY ( prepJob_id ) REFERENCES PREP_Job ( id );
ALTER TABLE PREP_Job ADD CONSTRAINT fk_primaryName_id_PREP_Job FOREIGN KEY ( primaryName_id ) REFERENCES PREP_JobName ( id );


# --- !Downs

ALTER TABLE PREP_JobName DROP FOREIGN KEY fk_prepJob_id_PREP_JobName;
ALTER TABLE PREP_Job DROP FOREIGN KEY fk_primaryName_id_PREP_Job;
DROP TABLE PREP_JobCategory;
DROP TABLE PREP_JobLevel;
DROP TABLE PREP_JobName;
DROP TABLE PREP_Job;