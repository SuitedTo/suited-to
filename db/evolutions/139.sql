# --- !Ups

CREATE TABLE CompanyJobStatus(
	id bigint(20) NOT NULL AUTO_INCREMENT,
	created datetime DEFAULT NULL,
	updated datetime DEFAULT NULL,
	company_id bigint(20) NOT NULL,
	status varchar(255) NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT CompanyJobStatus_Company FOREIGN KEY (company_id) REFERENCES Company (id)
);

ALTER TABLE Job ADD COLUMN status_id bigint(20);

ALTER TABLE Job ADD FOREIGN KEY (status_id) REFERENCES CompanyJobStatus (id);



# --- !Downs

DROP TABLE CompanyJobStatus;

ALTER TABLE Job DROP COLUMN status_id;