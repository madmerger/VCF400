INSERT INTO SETTINGS(SETTING, VALUE) VALUES ('ALWVOTE', 'Y'), ('PASSWORD', 'VCF400');
INSERT INTO AWARDDB(AWARDID, AWARDTITLE, AWARDDESC) VALUES
 (1, 'Best in Show Award', 'Best in Show Award'),
 (2, 'The Ed Fair Award', 'The Ed Fair Award');
INSERT INTO EXHBDB(EXHBDBID, EXHUSRPRF, EXHBITOR, EXHBCITY, EXHBSTATE, EXHBTITLE, EXHBDESC, ELIGIBLE, ENLRN400) VALUES
 (0, 'GERTIE', 'Gertie', 'Boston', 'MA', 'Gertie the AS/400', 'An exhibit about Gertie the AS/400.', 1, 1),
 (0, 'NOVOTE', 'No Vote', 'Boston', 'MA', 'No Vote Exhibit', 'This exhibit is not eligible for awards.', 0, 0),
 (0, 'BEEBOX', 'Bee Box', 'Boston', 'MA', 'Bee Box', 'A screen-play exhibit.', 1, 0);
INSERT INTO LRN400STR(OWNER, PAGENBR, CONTENT, EXTRA) VALUES
 ('LRN400STR', 1, 'Welcome to LEARN/400.', NULL),
 ('LRN400STR', 2, 'Use F5 to move forward and F8 to move back.', NULL),
 ('LRN400STR', 3, 'Programs may be called from a page.', NULL),
 ('LRN400STR', 4, 'JUMP', '0006'),
 ('LRN400STR', 5, 'The end is near.', NULL),
 ('LRN400STR', 6, 'Goodbye.', 'END'),
 ('GERTIE', 1, 'Gertie the AS/400 learning page one.', NULL),
 ('GERTIE', 2, 'Gertie the AS/400 learning page two.', 'END');
INSERT INTO BMOVDB(SPEAKER, LINE) VALUES
 ('BEE', 'Welcome to the Vintage Computer Festival.'),
 ('BEE', 'Please enjoy this short screenplay.'),
 ('GERTIE', 'I am an AS/400.'),
 ('BEE', 'That is a very capable computer.'),
 ('GERTIE', 'Thank you.');
INSERT INTO SECOFRS(USERPROF) VALUES ('MM2024');
