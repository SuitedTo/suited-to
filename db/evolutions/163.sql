
# --- !Ups

drop table FFMPEG_File; 
drop table FFMPEG_JobRequest;

alter table PREP_Question add column video_status varchar(255);

# --- !Downs

alter table PREP_Question drop column video_status;

CREATE TABLE `FFMPEG_JobRequest` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `client_tracking` varchar(255) DEFAULT NULL,
  `question_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `request_question` (`question_id`),
  CONSTRAINT `request_question` FOREIGN KEY (`question_id`) REFERENCES `PREP_Question` (`id`)
);



CREATE TABLE `FFMPEG_File` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `input_job_request_id` bigint(20) DEFAULT NULL,
  `output_job_request_id` bigint(20) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  `url` varchar(500) NOT NULL,
  `s3blob` varchar(500) DEFAULT NULL,
  `format` varchar(50) DEFAULT NULL,
  `codec` varchar(50) DEFAULT NULL,
  `duration` bigint(20) DEFAULT NULL,
  `file_size_limit` bigint(20) DEFAULT NULL,
  `start_time_offset` bigint(20) DEFAULT NULL,
  `time_stamp` bigint(20) DEFAULT NULL,
  `video_frames` bigint(20) DEFAULT NULL,
  `video_rate` double DEFAULT NULL,
  `video_size` varchar(50) DEFAULT NULL,
  `video_aspect_ratio` double DEFAULT NULL,
  `video_codec` varchar(50) DEFAULT NULL,
  `video_pass` bigint(20) DEFAULT NULL,
  `audio_frames` bigint(20) DEFAULT NULL,
  `audio_rate` double DEFAULT NULL,
  `audio_channels` bigint(20) DEFAULT NULL,
  `audio_codec` varchar(50) DEFAULT NULL,
  `audio_volume` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `input_owning_request` (`input_job_request_id`),
  KEY `output_owning_request` (`output_job_request_id`),
  CONSTRAINT `output_owning_request` FOREIGN KEY (`output_job_request_id`) REFERENCES `FFMPEG_JobRequest` (`id`),
  CONSTRAINT `input_owning_request` FOREIGN KEY (`input_job_request_id`) REFERENCES `FFMPEG_JobRequest` (`id`)
);

