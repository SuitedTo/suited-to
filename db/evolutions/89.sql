
# --- !Ups

UPDATE `DataTrigger` set propertyName='standardScore' WHERE triggerKey LIKE 'questionRatedFive';

UPDATE `DataTrigger` set propertyName='standardScore' WHERE triggerKey LIKE 'questionRatedFifty';

UPDATE `DataTrigger` set propertyName='standardScore' WHERE triggerKey LIKE 'questionRating';



# --- !Downs

UPDATE `DataTrigger` set propertyName='totalRating' WHERE triggerKey LIKE 'questionRatedFive';

UPDATE `DataTrigger` set propertyName='totalRating' WHERE triggerKey LIKE 'questionRatedFifty';

UPDATE `DataTrigger` set propertyName='totalRating' WHERE triggerKey LIKE 'questionRating';
