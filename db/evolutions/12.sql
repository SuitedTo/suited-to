# --- !Ups
ALTER TABLE Category ADD COLUMN question_count bigint(20);

ALTER TABLE Category ADD COLUMN creator_id bigint(20);

ALTER TABLE Category ADD FOREIGN KEY (creator_id) REFERENCES app_user (id);

ALTER TABLE Category ADD COLUMN status varchar(20);

UPDATE Category C SET
question_count = (select count(id) from QUESTION_CATEGORY where categories_id = C.id),
status = 'PUBLIC';

# --- !Downs
ALTER TABLE Category DROP COLUMN status;

ALTER TABLE Category DROP COLUMN creator_id;

ALTER TABLE Category DROP COLUMN question_count;