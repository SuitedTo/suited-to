# --- !Ups


ALTER TABLE UserBadge ADD COLUMN progress tinyint DEFAULT 0;

ALTER TABLE UserBadge ADD COLUMN earned datetime DEFAULT NULL;

ALTER TABLE UserBadge ADD COLUMN subjectIds VARCHAR(255) default NULL;

ALTER TABLE UserMetrics ADD COLUMN numberOfUsersInvited int(11) DEFAULT 0;



# --- !Downs


ALTER TABLE UserBadge DROP COLUMN progress;

ALTER TABLE UserBadge DROP COLUMN earned;

ALTER TABLE UserBadge DROP COLUMN subjectIds;

ALTER TABLE UserMetrics DROP COLUMN numberOfUsersInvited;
