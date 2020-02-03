
# --- !Ups

INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-13 14:55:33',NULL,1,'BADGES','numberOfAcceptedQuestions',NULL,'10','tenQuestionsAccepted','MODULO');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';

INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-14 05:46:04',NULL,1,'BADGES','numberOfLogins',NULL,'2','twoLogins','STANDARD');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';

INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-14 06:10:53',NULL,1,'BADGES','hasDisplayName',NULL,'true','displayNameCreated','STANDARD');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';

INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-14 12:17:34',NULL,1,'BADGES','hasProfilePicture',NULL,'true','profilePictureCreated','STANDARD');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';

INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-14 12:39:54',NULL,1,'BADGES','numberOfAcceptedQuestions',NULL,'1','firstQuestionAccepted','STANDARD');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';

INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-14 16:06:57',NULL,1,'BADGES','numberOfQuestionsRated',NULL,'1','firstQuestionRated','STANDARD');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';

INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-14 16:07:54',NULL,1,'BADGES','numberOfQuestionsRated',NULL,'10','tenQuestionsRated','MODULO');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';

INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-14 16:08:36',NULL,1,'BADGES','numberOfQuestionsReviewed',NULL,'1','firstQuestionReviewed','STANDARD');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';

INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-14 16:09:02',NULL,1,'BADGES','numberOfQuestionsReviewed',NULL,'10','tenQuestionsReviewed','MODULO');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';

INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-15 10:27:53',NULL,1,'BADGES','hrCompliant',NULL,'true','hrCompliant','STANDARD');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';


# --- !Downs
DELETE FROM DataTrigger;


