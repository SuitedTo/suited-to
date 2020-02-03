# --- !Ups

alter table CompanyJobStatus change name name varchar(255) default "To Be Screened";
INSERT INTO CompanyJobStatus (company_id) SELECT id from Company;

alter table CompanyJobStatus change name name varchar(255) default "Qualified";
INSERT INTO CompanyJobStatus (company_id) SELECT id from Company;

alter table CompanyJobStatus change name name varchar(255) default "Interviewing";
INSERT INTO CompanyJobStatus (company_id) SELECT id from Company;

alter table CompanyJobStatus change name name varchar(255) default "Disqualified";
INSERT INTO CompanyJobStatus (company_id) SELECT id from Company;

alter table CompanyJobStatus change name name varchar(255) default "Offered";
INSERT INTO CompanyJobStatus (company_id) SELECT id from Company;

alter table CompanyJobStatus change name name varchar(255) default "Hired";
INSERT INTO CompanyJobStatus (company_id) SELECT id from Company;

alter table CompanyJobStatus change name name varchar(255) default "To Be Screened";


# --- !Downs