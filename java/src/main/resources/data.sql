-- 初期データ: PUB400 ASHIBATA2 ライブラリの検証データと同一 (仕様書 3 章 参考データ)
MERGE INTO AWARDDB (AWARDID, AWARDTITLE, AWARDDESC) KEY (AWARDID) VALUES
  (1, 'Best Exhibit',   'Best overall exhibit at VCF'),
  (2, 'Best Vintage',   'Best vintage hardware exhibit'),
  (3, 'Peoples Choice', 'Attendee favorite');

MERGE INTO EXHBDB (EXHBDBID, EXHUSRPRF, EXHBITOR, EXHBCITY, EXHBSTATE, EXHBTITLE, EXHBDESC, ELIGIBLE, ENLRN400) KEY (EXHUSRPRF) VALUES
  (1, 'ASHIBATA', 'Akira Shibata',        'Tokyo',         'JP', 'IBM i on PUB400 Demo',                 'VCF/400 demo exhibit running on pub400.com', 1, 1),
  (2, 'DEMO400',  'Demo Exhibitor',       'Mountain View', 'CA', 'AS/400 Model 150',                     'A vintage AS/400 9401-150 exhibit',          1, 0),
  (3, 'NOVOTE',   'Ineligible Exhibitor', 'Atlanta',       'GA', 'Ineligible test exhibit (ELIGIBLE=0)', 'Test data for eligibility check',            0, 0);

MERGE INTO SETTINGS (SETTING, VALUE) KEY (SETTING) VALUES
  ('ADMPSWRD', 'VCF2024'),
  ('ALWVOTE',  'Y');

MERGE INTO LRN400STR (PAGENBR, CONTENT, EXTRA) KEY (PAGENBR) VALUES
  (1, 'Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit.', ''),
  (2, 'The AS/400 was introduced by IBM in June 1988 ... page 2', ''),
  (3, 'This is the last page. Thank you for visiting VCF/400.', 'END');

MERGE INTO VOTINGDB (BADGENBR, AWARDNBR, EXHBNBR) KEY (BADGENBR) VALUES
  (1,  1, 'ASHIBATA'),
  (28, 2, 'ASHIBATA');

MERGE INTO GUESTBKDB (CMTID, VISIBLE, EXHBID, GUESTNAME, GUESTCMT) KEY (CMTID) VALUES
  (1, 'Y', 'ASHIBATA', 'Great exhibit', 'VCF/400 running on PUB400.'),
  (2, 'Y', 'ASHIBATA', 'Devin',         'VCF/400 is running on PUB400.');
