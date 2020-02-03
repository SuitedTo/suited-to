# --- !Ups

UPDATE Category SET status = 'PUBLIC' where status = 'ACTIVE';

# --- !Downs

UPDATE Category SET status = 'ACTIVE' where status = 'PUBLIC';