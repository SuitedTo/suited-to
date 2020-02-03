# --- !Ups


CREATE TABLE `PREP_InterviewCategory` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `contribution` int(11) DEFAULT NULL,
  `difficulty` int(11) DEFAULT NULL,
  `prepCategory_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKA18253EDFB29B5B7` (`prepCategory_id`),
  CONSTRAINT `FKA18253EDFB29B5B7` FOREIGN KEY (`prepCategory_id`) REFERENCES `PREP_Category` (`id`)
);

CREATE TABLE `PREP_InterviewCategoryList` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
);


CREATE TABLE `PREP_InterviewCategoryListBuild` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `categoryList_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK154CBF036E4DFB1` (`categoryList_id`),
  CONSTRAINT `FK154CBF036E4DFB1` FOREIGN KEY (`categoryList_id`) REFERENCES `PREP_InterviewCategoryList` (`id`)
);


CREATE TABLE `PREP_InterviewCategory_PREP_InterviewCategoryList` (
  `list_id` bigint(20) NOT NULL,
  `category_id` bigint(20) NOT NULL,
  KEY `FK288B91BD323A7BD1` (`category_id`),
  KEY `FK288B91BDA1C8966F` (`list_id`),
  CONSTRAINT `FK288B91BDA1C8966F` FOREIGN KEY (`list_id`) REFERENCES `PREP_InterviewCategoryList` (`id`),
  CONSTRAINT `FK288B91BD323A7BD1` FOREIGN KEY (`category_id`) REFERENCES `PREP_InterviewCategory` (`id`)
);



# --- !Downs

DROP TABLE PREP_InterviewCategory;

DROP TABLE PREP_InterviewCategory_PREP_InterviewCategoryList;

DROP TABLE PREP_InterviewCategoryList;

DROP TABLE PREP_InterviewCategoryListBuild;

