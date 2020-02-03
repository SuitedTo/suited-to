# --- !Ups

ALTER TABLE app_user ADD COLUMN picturePublic bit(1) DEFAULT 1;
ALTER TABLE app_user ADD COLUMN badgesPublic bit(1) DEFAULT 1;
ALTER TABLE app_user ADD COLUMN contributingCategoriesPublic bit(1) DEFAULT 1;
ALTER TABLE app_user ADD COLUMN statusLevelPublic bit(1) DEFAULT 1;
ALTER TABLE app_user ADD COLUMN submittedQuestionsPublic bit(1) DEFAULT 1;
ALTER TABLE app_user ADD COLUMN reviewedQuestionsPublic bit(1) DEFAULT 1;
ALTER TABLE app_user ADD COLUMN canConductInterviews bit(1) DEFAULT 1;
ALTER TABLE app_user ADD COLUMN privacyLockdown bit(1) DEFAULT 0;


# --- !Downs

ALTER TABLE app_user DROP COLUMN picturePublic;
ALTER TABLE app_user DROP COLUMN badgesPublic;
ALTER TABLE app_user DROP COLUMN contributingCategoriesPublic;
ALTER TABLE app_user DROP COLUMN statusLevelPublic;
ALTER TABLE app_user DROP COLUMN submittedQuestionsPublic;
ALTER TABLE app_user DROP COLUMN reviewedQuestionsPublic;
ALTER TABLE app_user DROP COLUMN canConductInterviews;
ALTER TABLE app_user DROP COLUMN privacyLockdown;

