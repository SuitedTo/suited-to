# --- !Ups

ALTER TABLE Question change reviewStatus status varchar(255);

UPDATE Question SET status = 'PRIVATE' where privateQuestion = 1;


# --- !Downs

ALTER TABLE Question change status reviewStatus varchar(255);



