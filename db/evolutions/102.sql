# --- !Ups


CREATE TABLE `PREP_Interview` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `PREP_Question` (
  `id` bigint(20) NOT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `questionId` varchar(255) DEFAULT NULL,
  `interview_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3C3259D897AFFA50` (`interview_id`),
  CONSTRAINT `FK3C3259D897AFFA50` FOREIGN KEY (`interview_id`) REFERENCES `PREP_Interview` (`id`)
);


# --- !Downs

DROP TABLE PREP_Interview;

DROP TABLE PREP_Question;
