
# --- !Ups

ALTER TABLE PREP_Question ADD COLUMN video_uuid varchar(255);

# --- !Downs

ALTER TABLE PREP_Question DROP COLUMN video_uuid;