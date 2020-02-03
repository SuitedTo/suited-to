# --- !Ups

CREATE TABLE InvocationException
(
	id BIGINT PRIMARY KEY NOT NULL,
	created datetime DEFAULT NULL,
	updated datetime DEFAULT NULL,
	identifier VARCHAR(10),
	message TEXT
);


# --- !Downs

DROP TABLE InvocationException;