
# --- !Ups

INSERT INTO GateKeeper (id, restriction) VALUES (
        (SELECT value FROM ID_SEQ WHERE name = 'STANDARD_ENTITY' FOR UPDATE),
        'dashboardRefactor');
UPDATE ID_SEQ SET value = value + 1 WHERE name = 'STANDARD_ENTITY';

# --- !Downs
DELETE FROM GateKeeper WHERE restriction = 'dashboardRefactor';
