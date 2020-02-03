
# --- !Ups

CREATE TABLE ProInterviewerRequest
(
    id BIGINT PRIMARY KEY NOT NULL,
    created DATETIME,
    updated DATETIME,
    linkedInProfile VARCHAR(255),
    phone VARCHAR(255),
    status VARCHAR(255),
    supportingDocument TINYBLOB,
    yearsCategoryExperience VARCHAR(255),
    yearsInterviewerExperience VARCHAR(255),
    user_id BIGINT,
    FOREIGN KEY ( user_id ) REFERENCES app_user ( id )
);

CREATE TABLE ProInterviewerRequestFile
(
    id BIGINT PRIMARY KEY NOT NULL,
    created DATETIME,
    updated DATETIME,
    contents VARCHAR(255),
    name VARCHAR(255),
    type VARCHAR(255),
    proInterviewerRequest_id BIGINT,
    FOREIGN KEY ( proInterviewerRequest_id ) REFERENCES ProInterviewerRequest ( id )
);

CREATE TABLE prointerviewerrequest_category
(
    prointerviewerrequest_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    FOREIGN KEY ( prointerviewerrequest_id ) REFERENCES ProInterviewerRequest ( id ),
    FOREIGN KEY ( category_id ) REFERENCES Category ( id )
);



# --- !Downs

DROP TABLE prointerviewerrequest_category;
DROP TABLE ProInterviewerRequestFile;
DROP TABLE ProInterviewerRequest;


