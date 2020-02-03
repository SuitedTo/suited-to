# --- !Ups

CREATE TABLE Workflow (
    DTYPE varchar(255) DEFAULT NULL,
    id bigint(20) NOT NULL AUTO_INCREMENT,
    created datetime DEFAULT NULL,
    updated datetime DEFAULT NULL,
    comment varchar(255) DEFAULT NULL,
    status varchar(255) DEFAULT NULL,
    statusFrom varchar(255) DEFAULT NULL,
    question_id bigint(20) DEFAULT NULL,
    user_id bigint(20) DEFAULT NULL,
    category_id bigint(20) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY questionKey (question_id),
    CONSTRAINT questionKey FOREIGN KEY (question_id) REFERENCES Question(id),
    KEY userKey (user_id),
    CONSTRAINT userKey FOREIGN KEY (user_id) REFERENCES app_user(id),
    KEY categoryKey (category_id),
    CONSTRAINT categoryKey FOREIGN KEY (category_id) REFERENCES Category(id)
);

INSERT INTO Workflow(DTYPE, id, created, updated, comment, status, statusFrom, question_id, user_id, category_id)
    SELECT 'QuestionWorkflow', w.id, w.created, w.updated, w.comment, w.status, w.statusFrom, w.question_id, w.user_id, NULL
    FROM QuestionWorkflow w;


ALTER TABLE Notification DROP FOREIGN KEY FK2D45DD0B1709DE84;
ALTER TABLE Notification ADD COLUMN DTYPE varchar(255) DEFAULT NULL;
UPDATE Notification set DTYPE='QuestionNotification' where id>0;



DROP TABLE QuestionWorkflow;

# --- !Downs

DROP TABLE Workflow;
ALTER TABLE Notification DROP COLUMN DTYPE;

