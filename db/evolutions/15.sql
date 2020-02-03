
# --- !Ups

ALTER TABLE Interview ADD COLUMN categories varchar(255) DEFAULT null;


# --- !Downs


ALTER TABLE Interview DROP COLUMN categories;
