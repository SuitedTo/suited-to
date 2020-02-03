# --- !Ups


ALTER TABLE UserMetrics ADD COLUMN hasDisplayName bit(1) DEFAULT 0;

ALTER TABLE UserMetrics ADD COLUMN hasProfilePicture bit(1) DEFAULT 0;

ALTER TABLE UserMetrics ADD COLUMN numberOfLogins int(11) DEFAULT 0;

ALTER TABLE UserMetrics ADD COLUMN numberOfQuestionsReviewed int(11) DEFAULT 0;

ALTER TABLE UserMetrics ADD COLUMN numberOfQuestionsRated int(11) DEFAULT 0;

ALTER TABLE UserMetrics ADD COLUMN hrCompliant bit(1) DEFAULT 0;



# --- !Downs


ALTER TABLE UserMetrics DROP COLUMN hasDisplayName;

ALTER TABLE UserMetrics DROP COLUMN hasProfilePicture;

ALTER TABLE UserMetrics DROP COLUMN numberOfLogins;

ALTER TABLE UserMetrics DROP COLUMN numberOfQuestionsReviewed;

ALTER TABLE UserMetrics DROP COLUMN numberOfQuestionsRated;

ALTER TABLE UserMetrics DROP COLUMN hrCompliant;
