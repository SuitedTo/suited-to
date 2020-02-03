
# --- !Ups
UPDATE Question SET tips=TRIM(LEADING '\n' FROM CONCAT(tips, '\n', notes));
ALTER TABLE Question DROP COLUMN notes;

# --- !Downs
ALTER TABLE Question ADD COLUMN notes longtext;
