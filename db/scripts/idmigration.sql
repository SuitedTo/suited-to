--should be run manually prior to deploying corresponding version of the app

DROP PROCEDURE IF EXISTS idchange;

DELIMITER $$
CREATE PROCEDURE idchange ()
READS SQL DATA
BEGIN
    -- variables for processing
    DECLARE line varchar(255);

    -- Declare variables used just for cursor and loop control
    DECLARE no_more_rows BOOLEAN;
    DECLARE loop_cntr INT DEFAULT 0;
    DECLARE num_rows INT DEFAULT 0;

    -- Declare the cursor
    DECLARE cur1 CURSOR FOR
        SELECT CONCAT(
        'ALTER TABLE ', table_name,
        ' CHANGE id id bigint(20) NOT NULL') AS line
        FROM information_schema.columns
        WHERE table_schema = 'sparcin'
        AND column_name in ('id');




    -- Declare 'handlers' for exceptions
    DECLARE CONTINUE HANDLER FOR NOT FOUND
    SET no_more_rows = TRUE;


    -- 'open' the cursor and capture the number of rows returned
    -- (the 'select' gets invoked when the cursor is 'opened')
    OPEN cur1;
    select FOUND_ROWS() into num_rows;
    select num_rows;
    the_loop: LOOP

        FETCH  cur1
        INTO line;

        -- break out of the loop if
        -- 1) there were no records, or
        -- 2) we've processed them allCandidate
        IF no_more_rows THEN
            CLOSE cur1;
            LEAVE the_loop;
        END IF;

        set @a = line;
        prepare stmt from @a;
        execute stmt;

        -- count the number of times looped
        SET loop_cntr = loop_cntr + 1;

    END LOOP the_loop;

  -- 'print' the output so we can see they are the same
  select num_rows, loop_cntr;

END $$

call idchange();



SET foreign_key_checks = 0;

DROP PROCEDURE IF EXISTS idupdates;

DELIMITER $$
CREATE PROCEDURE idupdates ()
block1: BEGIN
    -- variables for processing
    DECLARE parent_table_val varchar(255);
    DECLARE prefix int(2) default(200);
    DECLARE no_more_rows BOOLEAN;

    DECLARE table_cursor CURSOR FOR
        SELECT TABLE_NAME
        FROM information_schema.columns
        WHERE table_schema = 'sparcin'
        AND column_name in ('id')
        AND table_name not in ('ActiveInterview', 'ActiveInterviewStateChange', 'ActiveInterviewEvent', 'play_evolutions');

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET no_more_rows = TRUE;

    OPEN table_cursor;
    parent_update_loop: LOOP
        FETCH  table_cursor
        INTO parent_table_val;

        set @a = CONCAT('UPDATE ', parent_table_val, ' SET id = CONCAT(', prefix, ', id)');
        prepare stmt FROM @a;
        select @a;
        execute stmt;

        IF no_more_rows THEN
            CLOSE table_cursor;
            LEAVE parent_update_loop;
        END IF;

        block2: BEGIN
            DECLARE child_update varchar(1000);
            DECLARE no_more_rows2 BOOLEAN;

            DECLARE update_cursor CURSOR FOR
                SELECT CONCAT('UPDATE ', TABLE_NAME, ' SET ', COLUMN_NAME, ' = CONCAT(', prefix, ', ', COLUMN_NAME, ')')
                FROM information_schema.KEY_COLUMN_USAGE
                WHERE REFERENCED_TABLE_NAME = parent_table_val
                AND REFERENCED_COLUMN_NAME = 'id';

            DECLARE CONTINUE HANDLER FOR NOT FOUND SET no_more_rows2 = TRUE;

            OPEN update_cursor;
            child_update_loop: LOOP
                FETCH update_cursor
                INTO child_update;
                -- some of these are going to be null if the parent table has no child tables that reference it
                IF child_update IS NOT NULL THEN
                    SET @b = child_update;
                     prepare stmt2 FROM @b;
                     select @b;
                     execute stmt2;
                select child_update;
                END IF;
                IF no_more_rows2 THEN
                    close update_cursor;
                    leave child_update_loop;
                end IF;
            END LOOP child_update_loop;
        END block2;

        SET prefix = prefix+1;
    END LOOP parent_update_loop;

END block1 $$

call idupdates();




-- manual corrections
update ActiveInterviewEvent set activeInterview_id = concat(210, activeInterview_id);
update Notification set workflow_id = concat(223, workflow_id); -- workflow_id didn't get updated as part of the procedure
update INTERVIEW_QUESTION set interview_id = substring(interview_id, 4, 15); -- interview_id had duplicated 209 prefix
update app_user set company_id = substring(company_id, 4, 15);
update Question set duplication_id = substring(duplication_id, 4, 15);
update question_metadata set question_id = substring(question_id, 4, 15);
update question_metadata set user_id = substring(user_id, 4, 15);
update question_metadata set id = substring(id, 4, 15);
update Feedback set candidateInterview_id = substring(candidateInterview_id, 4, 15);
update Feedback set activeInterview_id = concat(210, activeInterview_id);

