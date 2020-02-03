
# --- !Ups

ALTER TABLE Job MODIFY COLUMN id bigint(20) AUTO_INCREMENT;
ALTER TABLE Job add column description varchar(1000) default "This job is for any candidates that have not been assigned to a job within the company yet.";
ALTER TABLE Job change name name varchar(255) default "To Be Determined";

create table temp_store(id bigint(20));
INSERT INTO temp_store select max(id) from Job;

INSERT INTO Job (company_id) SELECT id from Company;
ALTER TABLE Job MODIFY COLUMN id bigint(20) NOT NULL;

UPDATE Job SET id = id+(SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE) where id > (select max(id) from temp_store);

drop table temp_store;

UPDATE ID_SEQ SET value = (select max(id) from Job) + 1;

# In 139.sql I created the CompanyJobStatus table with an auto increment ID and left it like that out of habit so I fix it here
ALTER TABLE CompanyJobStatus MODIFY COLUMN id bigint(20) NOT NULL;

UPDATE CompanyJobStatus SET id = id+(SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE);
UPDATE ID_SEQ SET value = (select max(id) from CompanyJobStatus) + 1;

# In 140.sql I created the CandidateJobStatus table with an auto increment ID and left it like that out of habit so I fix it here
ALTER TABLE CandidateJobStatus MODIFY COLUMN id bigint(20) NOT NULL;


# --- !Downs

ALTER TABLE Job drop column description;