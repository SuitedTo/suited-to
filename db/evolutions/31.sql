
# --- !Ups

ALTER TABLE app_user ADD COLUMN questionStatusEmails bit(1) DEFAULT null;

UPDATE app_user set questionStatusEmails = 1;



# --- !Downs


ALTER TABLE app_user DROP COLUMN questionStatusEmails;
