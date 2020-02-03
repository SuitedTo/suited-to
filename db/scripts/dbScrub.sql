#clear out sensitive company info
update Company set lastFourCardDigits = null, paymentSystemKey = null, phoneNumberValue=null, contactEmail=null, address=null where id > 0;

#update email to replace the top level domain with 'test'
UPDATE app_user SET email=REPLACE(email, SUBSTRING_INDEX(email, '.', -1), 'test') where id > 0;

#update full name to Blowy
UPDATE app_user SET fullName = REPLACE(fullName, SUBSTRING_INDEX(fullName, ' ', 1), 'Blowy') where id > 0;

#fix up the password to have everyone be googlerules
UPDATE app_user SET salt = '$2a$10$XFKhwOQUmqsqtv/j33HgUO', password = 'IEGG0IQcGGHOHKEXkB6SSqPOAT+x8S0xJ/SSIdVfnltrdzhjbbpzJEXMMGUwtGM6dcV0P6kl1YBYiD/iIcT+pw==' where id > 0;

#remove other stuff
UPDATE app_user SET googleOpenIdUrl = null, googleOpenIdEmail = null, picture = null, thumbnail = null where id > 0;

#delete candidate files
DELETE from CandidateFile where id > 0;

DELETE from Feedback where id > 0;

UPDATE Candidate_emails set emailAddress = 'test@example.com' where Candidate_id > 0;

UPDATE Candidate_externalLinks set externalLinkValue = 'http://www.google.com' where Candidate_id > 0;

UPDATE Candidate_phoneNumbers set phoneNumberValue = '(555) 555-5555' where Candidate_id > 0;

#update candidate info
UPDATE Candidate set name = 'Blowy McBlowerson', taleoCandId = null where id > 0;

#update live interviews which often have candidate names
update Interview i
set name = 'Candidate Interview'
where exists(select * from ActiveInterview ai where ai.id = i.id)
and i.id > 0;
