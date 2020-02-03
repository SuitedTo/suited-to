# --- !Ups

alter table CompanyJobStatus CHANGE status name varchar(255) NOT NULL;
ALTER TABLE Job DROP FOREIGN KEY Job_ibfk_1;
ALTER TABLE Job DROP COLUMN status_id;

CREATE TABLE CandidateJobStatus(
	id bigint(20) NOT NULL AUTO_INCREMENT,
	created datetime DEFAULT NULL,
	updated datetime DEFAULT NULL,
	job_id bigint(20) NOT NULL,
	candidate_id bigint(20) NOT NULL,
	companyJobStatus_id bigint(20) NOT NULL,
	PRIMARY KEY (id),
	CONSTRAINT CandidateJobStatus_Job FOREIGN KEY (job_id) REFERENCES Job (id),
	CONSTRAINT CandidateJobStatus_Candidate FOREIGN KEY (candidate_id) REFERENCES Candidate (id),
	CONSTRAINT CandidateJobStatus_CompanyJobStatus FOREIGN KEY (companyJobStatus_id) REFERENCES CompanyJobStatus (id)
);

# --- !Downs

ALTER TABLE Job ADD COLUMN status_id bigint(20);
alter table CompanyJobStatus CHANGE name status varchar(255) NOT NULL;
ALTER TABLE Job ADD FOREIGN KEY (status_id) REFERENCES CompanyJobStatus (id);
DROP TABLE CandidateJobStatus;