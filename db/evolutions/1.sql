# Users schema

# --- !Ups

-- ----------------------------
--  Table structure for AvailableSecurityQuestion
-- ----------------------------
CREATE TABLE AvailableSecurityQuestion (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  question varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
--  Table structure for Company
-- ----------------------------
CREATE TABLE Company (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  address varchar(255) DEFAULT NULL,
  contactEmail varchar(255) DEFAULT NULL,
  contactJobTitle varchar(255) DEFAULT NULL,
  contactName varchar(255) DEFAULT NULL,
  phoneNumberType int(11) DEFAULT NULL,
  phoneNumberValue varchar(255) DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  privateQuestionEnabled bit(1) NOT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
--  Table structure for app_user
-- ----------------------------
CREATE TABLE app_user (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  answerKey varchar(255) DEFAULT NULL,
  displayName varchar(255) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  fullName varchar(255) DEFAULT NULL,
  invitationKey varchar(255) DEFAULT NULL,
  password varchar(255) DEFAULT NULL,
  passwordExpiration datetime DEFAULT NULL,
  salt varchar(255) DEFAULT NULL,
  status int(11) DEFAULT NULL,
  superReviewer bit(1) NOT NULL,
  company_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY displayName (displayName),
  UNIQUE KEY email (email),
  KEY FK459C57291366CCF6 (company_id),
  CONSTRAINT FK459C57291366CCF6 FOREIGN KEY (company_id) REFERENCES Company (id)
);

-- ----------------------------
--  Table structure for Job
-- ----------------------------
CREATE TABLE Job (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  company_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK1239D1366CCF6 (company_id),
  CONSTRAINT FK1239D1366CCF6 FOREIGN KEY (company_id) REFERENCES Company (id)
);

-- ----------------------------
--  Table structure for Candidate
-- ----------------------------
CREATE TABLE Candidate (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  address varchar(255) DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  status int(11) DEFAULT NULL,
  company_id bigint(20) DEFAULT NULL,
  job_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK95C3B5631366CCF6 (company_id),
  KEY FK95C3B563399E12F6 (job_id),
  CONSTRAINT FK95C3B563399E12F6 FOREIGN KEY (job_id) REFERENCES Job (id),
  CONSTRAINT FK95C3B5631366CCF6 FOREIGN KEY (company_id) REFERENCES Company (id)
);

-- ----------------------------
--  Table structure for Interview
-- ----------------------------
CREATE TABLE Interview (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  time int(11) DEFAULT NULL,
  company_id bigint(20) DEFAULT NULL,
  user_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  KEY FK956EF1611366CCF6 (company_id),
  KEY FK956EF16147140EFE (user_id),
  CONSTRAINT FK956EF16147140EFE FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT FK956EF1611366CCF6 FOREIGN KEY (company_id) REFERENCES Company (id)
);

-- ----------------------------
--  Table structure for CandidateInterview
-- ----------------------------

CREATE TABLE CandidateInterview (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  date datetime DEFAULT NULL,
  candidate_id bigint(20) DEFAULT NULL,
  interview_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK306D9B5EA1A3D776 (interview_id),
  KEY FK306D9B5E2A3E4036 (candidate_id),
  CONSTRAINT FK306D9B5E2A3E4036 FOREIGN KEY (candidate_id) REFERENCES Candidate (id),
  CONSTRAINT FK306D9B5EA1A3D776 FOREIGN KEY (interview_id) REFERENCES Interview (id)
);

-- ----------------------------
--  Table structure for Candidate_emails
-- ----------------------------

CREATE TABLE Candidate_emails (
  Candidate_id bigint(20) NOT NULL,
  emailAddress varchar(255) DEFAULT NULL,
  emailType int(11) DEFAULT NULL,
  KEY FKADA22AB32A3E4036 (Candidate_id),
  CONSTRAINT FKADA22AB32A3E4036 FOREIGN KEY (Candidate_id) REFERENCES Candidate (id)
);

-- ----------------------------
--  Table structure for Candidate_externalLinks
-- ----------------------------

CREATE TABLE Candidate_externalLinks (
  Candidate_id bigint(20) NOT NULL,
  externalLinkType int(11) DEFAULT NULL,
  externalLinkValue varchar(255) DEFAULT NULL,
  KEY FK767F80D22A3E4036 (Candidate_id),
  CONSTRAINT FK767F80D22A3E4036 FOREIGN KEY (Candidate_id) REFERENCES Candidate (id)
);

-- ----------------------------
--  Table structure for Candidate_phoneNumbers
-- ----------------------------

CREATE TABLE Candidate_phoneNumbers (
  Candidate_id bigint(20) NOT NULL,
  phoneNumberType int(11) DEFAULT NULL,
  phoneNumberValue varchar(255) DEFAULT NULL,
  KEY FK2DC15E582A3E4036 (Candidate_id),
  CONSTRAINT FK2DC15E582A3E4036 FOREIGN KEY (Candidate_id) REFERENCES Candidate (id)
);

-- ----------------------------
--  Table structure for Category
-- ----------------------------

CREATE TABLE Category (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);



-- ----------------------------
--  Table structure for INTERVIEW_CATEGORY
-- ----------------------------

CREATE TABLE INTERVIEW_CATEGORY (
  Interview_id bigint(20) NOT NULL,
  categories_id bigint(20) NOT NULL,
  KEY FKE5B9FA1C97B6E640 (categories_id),
  KEY FKE5B9FA1CA1A3D776 (Interview_id),
  CONSTRAINT FKE5B9FA1CA1A3D776 FOREIGN KEY (Interview_id) REFERENCES Interview (id),
  CONSTRAINT FKE5B9FA1C97B6E640 FOREIGN KEY (categories_id) REFERENCES Category (id)
);

-- ----------------------------
--  Table structure for Question
-- ----------------------------
CREATE TABLE Question (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  answers longtext,
  difficulty varchar(255) DEFAULT NULL,
  notes longtext,
  privateQuestion bit(1) NOT NULL,
  reviewStatus varchar(255) DEFAULT NULL,
  text varchar(255) DEFAULT NULL,
  time varchar(255) DEFAULT NULL,
  tips longtext,
  user_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  KEY FKBE5CA00647140EFE (user_id),
  CONSTRAINT FKBE5CA00647140EFE FOREIGN KEY (user_id) REFERENCES app_user (id)
);

-- ----------------------------
--  Table structure for INTERVIEW_QUESTION
-- ----------------------------

CREATE TABLE INTERVIEW_QUESTION (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  comment varchar(255) DEFAULT NULL,
  sortOrder int(11) DEFAULT NULL,
  interview_id bigint(20) DEFAULT NULL,
  question_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK9D3979048F77231E (question_id),
  KEY FK9D397904A1A3D776 (interview_id),
  CONSTRAINT FK9D397904A1A3D776 FOREIGN KEY (interview_id) REFERENCES Interview (id),
  CONSTRAINT FK9D3979048F77231E FOREIGN KEY (question_id) REFERENCES Question (id)
);



-- ----------------------------
--  Table structure for JOB_CATEGORY
-- ----------------------------

CREATE TABLE JOB_CATEGORY (
  Job_id bigint(20) NOT NULL,
  categories_id bigint(20) NOT NULL,
  KEY FK5DE9FF6097B6E640 (categories_id),
  KEY FK5DE9FF60399E12F6 (Job_id),
  CONSTRAINT FK5DE9FF60399E12F6 FOREIGN KEY (Job_id) REFERENCES Job (id),
  CONSTRAINT FK5DE9FF6097B6E640 FOREIGN KEY (categories_id) REFERENCES Category (id)
);



-- ----------------------------
--  Table structure for QUESTION_CATEGORY
-- ----------------------------

CREATE TABLE QUESTION_CATEGORY (
  Question_id bigint(20) NOT NULL,
  categories_id bigint(20) NOT NULL,
  KEY FKEAE1B53797B6E640 (categories_id),
  KEY FKEAE1B5378F77231E (Question_id),
  CONSTRAINT FKEAE1B5378F77231E FOREIGN KEY (Question_id) REFERENCES Question (id),
  CONSTRAINT FKEAE1B53797B6E640 FOREIGN KEY (categories_id) REFERENCES Category (id)
);



-- ----------------------------
--  Table structure for QuestionComment
-- ----------------------------

CREATE TABLE QuestionComment (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  text longtext,
  question_id bigint(20) DEFAULT NULL,
  workflow_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK6954F3798F77231E (question_id),
  CONSTRAINT FK6954F3798F77231E FOREIGN KEY (question_id) REFERENCES Question (id)
);

-- ----------------------------
--  Table structure for QuestionWorkflow
-- ----------------------------

CREATE TABLE QuestionWorkflow (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  status varchar(255) DEFAULT NULL,
  statusFrom varchar(255) DEFAULT NULL,
  comment_id bigint(20) DEFAULT NULL,
  question_id bigint(20) DEFAULT NULL,
  user_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FKE74D75E547140EFE (user_id),
  KEY FKE74D75E5ADB01AF0 (comment_id),
  KEY FKE74D75E58F77231E (question_id),
  CONSTRAINT FKE74D75E58F77231E FOREIGN KEY (question_id) REFERENCES Question (id),
  CONSTRAINT FKE74D75E547140EFE FOREIGN KEY (user_id) REFERENCES app_user (id),
  CONSTRAINT FKE74D75E5ADB01AF0 FOREIGN KEY (comment_id) REFERENCES QuestionComment (id)
);

ALTER TABLE QuestionComment ADD FOREIGN KEY (workflow_id) REFERENCES QuestionWorkflow (id);

-- ----------------------------
--  Table structure for SecurityQuestion
-- ----------------------------

CREATE TABLE SecurityQuestion (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created datetime DEFAULT NULL,
  updated datetime DEFAULT NULL,
  answer varchar(255) DEFAULT NULL,
  question varchar(255) DEFAULT NULL,
  user_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY user_id (user_id,question),
  KEY FKF3681B6647140EFE (user_id),
  CONSTRAINT FKF3681B6647140EFE FOREIGN KEY (user_id) REFERENCES app_user (id)
);

-- ----------------------------
--  Table structure for User_roles
-- ----------------------------

CREATE TABLE User_roles (
  User_id bigint(20) NOT NULL,
  roles int(11) DEFAULT NULL,
  KEY FKEA14756947140EFE (User_id),
  CONSTRAINT FKEA14756947140EFE FOREIGN KEY (User_id) REFERENCES app_user (id)
);


-- ----------------------------
--  Table structure for app_user_Category
-- ----------------------------

CREATE TABLE app_user_Category (
  User_id bigint(20) NOT NULL,
  reviewCategories_id bigint(20) NOT NULL,
  UNIQUE KEY reviewCategories_id (reviewCategories_id),
  KEY FK6B5AF97447140EFE (User_id),
  KEY FK6B5AF974DFEE9DE8 (reviewCategories_id),
  CONSTRAINT FK6B5AF974DFEE9DE8 FOREIGN KEY (reviewCategories_id) REFERENCES Category (id),
  CONSTRAINT FK6B5AF97447140EFE FOREIGN KEY (User_id) REFERENCES app_user (id)
);

-- ----------------------------
--  Table structure for question_metadata
-- ----------------------------

CREATE TABLE question_metadata (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  rating int(11) DEFAULT NULL,
  question_id bigint(20) DEFAULT NULL,
  user_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK495E736847140EFE (user_id),
  KEY FK495E73688F77231E (question_id),
  CONSTRAINT FK495E73688F77231E FOREIGN KEY (question_id) REFERENCES Question (id),
  CONSTRAINT FK495E736847140EFE FOREIGN KEY (user_id) REFERENCES app_user (id)
);


# --- !Downs

DROP TABLE User;