# --- !Ups

CREATE TABLE FFMPEG_JobRequest
(
	id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
	created datetime DEFAULT NULL,
	updated datetime DEFAULT NULL,
	json_blob varchar(255) DEFAULT NULL
);

# --- !Downs

DROP TABLE FFMPEG_JobRequest;