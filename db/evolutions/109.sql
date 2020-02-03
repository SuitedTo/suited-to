# --- !Ups

CREATE TABLE `PREP_InterviewBuild` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `interview_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK58622DDF97AFFA50` (`interview_id`),
  CONSTRAINT `FK58622DDF97AFFA50` FOREIGN KEY (`interview_id`) REFERENCES `PREP_Interview` (`id`)
);


# --- !Downs

DROP TABLE PREP_InterviewBuild;