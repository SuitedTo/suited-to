# --- !Ups
update Question set standardScore = 0 where standardScore is null;
alter table Question alter column standardscore set default 0;
alter table Question modify standardScore int(11) not null;

# --- !Downs
alter table Question modify standardScore int(11) null;
alter table Question alter column standardscore drop default;
update Question set standardScore = null where standardScore = 0;