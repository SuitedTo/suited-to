# --- !Ups

ALTER TABLE Job ADD CONSTRAINT unique_name UNIQUE(name, company_id);

# --- !Downs

ALTER TABLE Company DROP CONSTRAINT unique_name;