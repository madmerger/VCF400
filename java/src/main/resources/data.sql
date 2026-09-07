INSERT INTO SETTINGS(SETTING, VALUE) VALUES ('ALWVOTE', 'Y'), ('PASSWORD', 'VCF2024');
INSERT INTO AWARDDB(AWARDID, AWARDTITLE, AWARDDESC) VALUES
 (1, 'Best in Show Award', 'Best in Show Award'),
 (2, 'The Ed Fair Award', 'The Ed Fair Award'),
 (3, 'Peoples Choice Award', 'Peoples Choice Award');
INSERT INTO EXHBDB(EXHBDBID, EXHUSRPRF, EXHBITOR, EXHBCITY, EXHBSTATE, EXHBTITLE, EXHBDESC, ELIGIBLE, ENLRN400) VALUES
 (1, 'ASHIBATA', 'Akira Shibata', 'Boston', 'MA', 'IBM i on PUB400 Demo', 'An IBM i exhibit running on PUB400.', 1, 1),
 (2, 'DEMO400', 'Demo 400', 'Austin', 'TX', 'AS/400 Model 150', 'An AS/400 Model 150 exhibit.', 1, 0),
 (3, 'NOVOTE', 'No Vote', 'Boston', 'MA', 'No Vote Exhibit', 'This exhibit is not eligible for awards.', 0, 0);
INSERT INTO LRN400STR(OWNER, PAGENBR, CONTENT, EXTRA) VALUES
 ('LRN400STR', 1, 'Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit.', NULL),
 ('LRN400STR', 2, 'The AS/400 was introduced by IBM in June 1988 and became a popular business computer platform. This is page 2.', NULL),
 ('LRN400STR', 3, 'This is the last page. Thank you for visiting VCF/400.', 'END');
INSERT INTO LRN400STR(OWNER, PAGENBR, CONTENT, EXTRA) VALUES
 ('DEMOJUMP', 1, 'JUMP test start.', NULL),
 ('DEMOJUMP', 2, 'JUMP', '0004'),
 ('DEMOJUMP', 3, 'CALL', 'NTRSTIT'),
 ('DEMOJUMP', 4, 'JUMP test end.', 'END');
INSERT INTO BMOVDB(SPEAKER, LINE) VALUES
 ('BEE', 'Welcome to the Vintage Computer Festival.'),
 ('BEE', 'Please enjoy this short screenplay.'),
 ('ASHIBATA', 'I am an AS/400.'),
 ('BEE', 'That is a very capable computer.'),
 ('ASHIBATA', 'Thank you.');
INSERT INTO SECOFRS(USERPROF) VALUES ('MM2024');
