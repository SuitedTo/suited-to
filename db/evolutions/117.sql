# --- !Ups
ALTER TABLE PREP_Interview ADD COLUMN owner_id bigint(20) NOT NULL;

ALTER TABLE PREP_Interview ADD CONSTRAINT prepinterview_prepuser
        FOREIGN KEY (owner_id) REFERENCES PREP_User(id);
        
ALTER TABLE PREP_Question ADD COLUMN staticAnswers varchar(255) DEFAULT null;

# --- !Downs

ALTER TABLE PREP_Interview DROP FOREIGN KEY prepinterview_prepuser;
        
ALTER TABLE PREP_Interview DROP COLUMN owner_id;

ALTER TABLE PREP_Question DROP COLUMN staticAnswers;