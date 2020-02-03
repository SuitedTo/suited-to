# --- !Ups

CREATE TABLE PREP_InterviewReview
(
	id BIGINT PRIMARY KEY NOT NULL,
    created DATETIME,
    updated DATETIME,
    prepInterview_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    reviewKey VARCHAR(255) NOT NULL,
    reviewerEmail  VARCHAR(255) NOT NULL,
    FOREIGN KEY ( prepInterview_id ) REFERENCES PREP_Interview ( id )
);

CREATE TABLE PREP_QuestionReview
(
	id BIGINT PRIMARY KEY NOT NULL,
    created DATETIME,
    updated DATETIME,
    prepQuestion_id BIGINT NOT NULL,
    prepInterviewReview_id BIGINT NOT NULL,
    text  VARCHAR(1000),
    FOREIGN KEY ( prepQuestion_id ) REFERENCES PREP_Question ( id ),
    FOREIGN KEY ( prepInterviewReview_id ) REFERENCES PREP_InterviewReview ( id )
);


# --- !Downs

DROP TABLE PREP_InterviewReview;
DROP TABLE PREP_QuestionReview;
