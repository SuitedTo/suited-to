# --- !Ups
ALTER TABLE app_user ADD COLUMN hasViewedIntroduction bit(1) NOT NULL;
UPDATE app_user SET hasViewedIntroduction = 1;

# --- !Downs
ALTER TABLE app_user DROP COLUMN hasViewedIntroduction;