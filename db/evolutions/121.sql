# --- !Ups

CREATE TABLE PREP_JobCategory_TEMP (
  PREP_JobCategory_id BIGINT,
  prepJobLevel_name VARCHAR(255),
  difficulty BIGINT,
  weight BIGINT
);

INSERT INTO PREP_JobCategory_TEMP(PREP_JobCategory_id, prepJobLevel_name, difficulty, weight)
  SELECT  c.id, l.name, c.difficulty, c.weight FROM PREP_JobCategory c LEFT JOIN PREP_JobLevel l
      ON c.prepJobLevel_id = l.id;

ALTER TABLE PREP_JobCategory DROP FOREIGN KEY fk_prepJobLevel_id_PREP_JobCategory;

ALTER TABLE PREP_JobCategory DROP COLUMN prepJobLevel_id;

ALTER TABLE PREP_JobCategory DROP COLUMN difficulty;

ALTER TABLE PREP_JobCategory DROP COLUMN weight;

ALTER TABLE PREP_JobCategory ADD COLUMN prepJobLevel varchar(20);

ALTER TABLE PREP_JobCategory ADD COLUMN difficulty varchar(20);

ALTER TABLE PREP_JobCategory ADD COLUMN weight varchar(20);

UPDATE PREP_JobCategory jc JOIN PREP_JobCategory_TEMP tmp
    ON jc.id = tmp.PREP_JobCategory_id
SET jc.prepJobLevel = 'ENTRY'
WHERE tmp.prepJobLevel_name = 'Junior';

UPDATE PREP_JobCategory jc JOIN PREP_JobCategory_TEMP tmp
    ON jc.id = tmp.PREP_JobCategory_id
SET jc.prepJobLevel = 'MID'
WHERE tmp.prepJobLevel_name = 'Mid';

UPDATE PREP_JobCategory jc JOIN PREP_JobCategory_TEMP tmp
    ON jc.id = tmp.PREP_JobCategory_id
SET jc.prepJobLevel = 'SENIOR'
WHERE tmp.prepJobLevel_name = 'Senior';

UPDATE PREP_JobCategory jc JOIN PREP_JobCategory_TEMP tmp
    ON jc.id = tmp.PREP_JobCategory_id
SET jc.difficulty = 'EASY'
WHERE tmp.difficulty = 1;

UPDATE PREP_JobCategory jc JOIN PREP_JobCategory_TEMP tmp
    ON jc.id = tmp.PREP_JobCategory_id
SET jc.difficulty = 'MEDIUM'
WHERE tmp.difficulty = '2';

UPDATE PREP_JobCategory jc JOIN PREP_JobCategory_TEMP tmp
    ON jc.id = tmp.PREP_JobCategory_id
SET jc.difficulty = 'HARD'
WHERE tmp.difficulty = '3';

UPDATE PREP_JobCategory jc JOIN PREP_JobCategory_TEMP tmp
    ON jc.id = tmp.PREP_JobCategory_id
SET jc.weight = 'SMALL'
WHERE tmp.weight = 1;

UPDATE PREP_JobCategory jc JOIN PREP_JobCategory_TEMP tmp
    ON jc.id = tmp.PREP_JobCategory_id
SET jc.weight = 'MEDIUM'
WHERE tmp.weight = '2';

UPDATE PREP_JobCategory jc JOIN PREP_JobCategory_TEMP tmp
    ON jc.id = tmp.PREP_JobCategory_id
SET jc.weight = 'LARGE'
WHERE tmp.weight = '3';

DROP TABLE PREP_JobLevel;

# --- !Downs
