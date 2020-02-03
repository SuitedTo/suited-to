# --- !Ups

CREATE TABLE ActiveInterviewEvent (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    created datetime DEFAULT NULL,
    updated datetime DEFAULT NULL,
    activeInterview_id bigint(20) NOT NULL,
    initiatingUser_id bigint(20) NOT NULL,
    PRIMARY KEY (id),
    KEY activeInterviewEventActiveInterviewIsActiveInterview
            (activeInterview_id),
    CONSTRAINT activeInterviewEventActiveInterviewIsActiveInterview
            FOREIGN KEY (activeInterview_id) REFERENCES ActiveInterview(id),
    KEY activeInterviewEventInitiatingUserIsUser (initiatingUser_id),
    CONSTRAINT activeInterviewEventInitiatingUserIsUser
            FOREIGN KEY (initiatingUser_id) REFERENCES app_user(id)
);

CREATE TABLE ActiveInterviewStateChange (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    toState varchar(255) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY activeInterviewStateChangeIdIsActiveInterviewEvent (id),
    CONSTRAINT activeInterviewStateChangeIdIsActiveInterviewEvent
            FOREIGN KEY (id) REFERENCES ActiveInterviewEvent(id)
);


# --- !Downs

DROP TABLE ActiveInterviewEvent;
DROP TABLE ActiveInterviewStateChange;
