
# --- !Ups


INSERT INTO `DataTrigger` (`id`,`created`,`updated`,`active`,`domain`,`propertyName`,`subdomain`,`targetValue`,`triggerKey`,`type`) VALUES ((SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),'2012-11-13 14:55:33',NULL,1,'BADGES','status',NULL,'PUBLIC','publicCategory','STANDARD');

UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';


# --- !Downs

DELETE FROM DataTrigger WHERE triggerKey LIKE 'publicCategory';
