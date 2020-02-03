# --- !Ups

alter table Question modify text VARCHAR(1000);

# --- !Downs

alter table Question modify text VARCHAR(255);