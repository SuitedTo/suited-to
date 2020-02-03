
# --- !Ups
ALTER TABLE app_user ADD COLUMN hrCompliant bit(1) NOT NULL DEFAULT 0;

# --- !Downs

ALTER TABLE app_user DROP COLUMN hrCompliant;
