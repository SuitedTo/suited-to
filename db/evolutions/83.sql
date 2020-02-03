
# --- !Ups


INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-13 14:55:33',NULL,1,'USER_EVENTS','numberOfSubmittedQuestions','QUESTIONS_SUBMITTED','5','questionsSubmitted','STANDARD');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';

INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-13 14:55:33',NULL,1,'USER_EVENTS','totalRating','QUESTION_RATING','50','questionRating','MODULO');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';

ALTER TABLE UserMetrics ADD COLUMN numberOfSubmittedQuestions int(11) DEFAULT 0;

# --- !Downs

DELETE FROM DataTrigger WHERE triggerKey LIKE 'questionsSubmitted';

ALTER TABLE UserMetrics DROP COLUMN numberOfSubmittedQuestions;
