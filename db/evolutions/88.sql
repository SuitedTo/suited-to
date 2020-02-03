
# --- !Ups


UPDATE ID_SEQ SET value = value/1000 + 1 WHERE name = 'STANDARD_ENTITY';

# --- !Downs


UPDATE ID_SEQ SET value = value*1000 WHERE name = 'STANDARD_ENTITY';
