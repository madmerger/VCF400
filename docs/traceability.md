# フェーズ2 トレーサビリティ

仕様書の BR-ID と Java の対応を以下に示す。RPG の 1 プログラムを原則 1 サービスクラスへ対応させ、画面処理は REST と Thymeleaf UI の両方から利用できるようにした。

## BR-ID 対応表

| RPGプログラム | サブルーチン/Javaメソッド | BR-ID | Javaクラス | RESTエンドポイント | UIパス |
|---|---|---|---|---|---|
| `ADDVOTE` | `CHKPARM`/`CHKALWVOTE`/`submit` | `BR-ADDVOTE-01`～`BR-ADDVOTE-09` | `AddVoteService#submit` | `POST /api/vote` | `/vote?profile=` |
| `ADDGBCMT` | `CHKPARM`/`ADDTODB`/`ENDCMT` | `BR-ADDGBCMT-01`～`BR-ADDGBCMT-05` | `AddGuestbookCommentService#submit` | `POST /api/guestbook` | `/guestbook/add?profile=` |
| `READGBCMT` | `GETTLCMT`/`READDB` | `BR-READGBCMT-01`～`BR-READGBCMT-05` | `ReadGuestbookCommentService#read` | `GET /api/guestbook/{id}` | `/guestbook/read?profile=` |
| `LRN400` | `PARSESCRIPT`/`PRFRMACTN`/`JUMPTO`/`PAGEFWD`/`PAGEBACK`/`CHKPARM` | `BR-LRN400-01`～`BR-LRN400-05` | `Learn400Service#start/pageFwd/pageBack` | `GET /api/learn400/{owner}[/{fwd|back}]` | `/learn400?owner=` |
| `LRN400AUT` | `PAGEFWD`/`PAGEBACK` | `BR-LRN400AUT-01`～`BR-LRN400AUT-04` | `Learn400AutoService#start/pageFwd/pageBack` | `GET /api/learn400/auto/{owner}` | `/learn400/auto` |
| `ADMLRN400` | `PAGEFWD`/`PAGEBACK`/`WRITERCD`/`UPDRCD`/`CRTRCD` | `BR-ADMLRN400-01`～`BR-ADMLRN400-05` | `AdminLearn400Service#start/pageFwd/pageBack/save` | `GET/POST /api/admin/learn400` | `/admin/learn400` |
| `EXHBMENU` | `GETEXHB`/`DOVOTE`/`DOLRN400`/`ADMKIOSK`/`GETPSWRD` | `BR-EXHBMENU-01`～`BR-EXHBMENU-07` | `ExhibitMenuService#load/select/exitKiosk` | `GET /api/menu/{profile}` | `/menu?profile=` |
| `ADMVOTERPT` | `READVOTE`/`CHKPARM` | `BR-ADMVOTERPT-01`～`BR-ADMVOTERPT-04` | `AdminVoteReportService#readVote` | `GET /api/admin/votes/{profile}` | `/admin/votes` |
| `ADMCRTEXHB` | `LOADEXHB`/`WRITEEXHB`/`DELRCD`/`CHKOFRS`/`CHKPARM` | `BR-ADMCRTEXHB-01`～`BR-ADMCRTEXHB-07` | `AdminExhibitService#loadSession/writeExhb/delRcd` | `GET/POST/DELETE /api/admin/exhibits` | `/admin/exhibits` |
| `ADMHIDECMT` | `READDB`/`HIDECMTSR`/`CHKUSRPRF`/`CHKPARM` | `BR-ADMHIDECMT-01`～`BR-ADMHIDECMT-04` | `AdminHideCommentService#read/list/update` | `GET /api/guestbook`, `POST /api/admin/comments/{id}/visibility` | `/admin/comments` |
| `ADMADDSOFR`/`ADMOFRLIST` | `ADDTODB`/一覧 | `BR-ADMADDSOFR-01`～`BR-ADMADDSOFR-02` | `AdminSecurityOfficerService#add/list` | `GET/POST /api/admin/officers` | `/admin/officers` |
| `ADMSETTING` | `SETUPMAIN`/`ADDTODB` | `BR-ADMSETTING-01`～`BR-ADMSETTING-03` | `AdminSettingService#load/save` | `GET/POST /api/admin/settings` | `/admin/settings` |
| `BEEMOVIE` | `CLEARSR`/`LOADSR` | `BR-BEEMOVIE-01`～`BR-BEEMOVIE-02` | `BeeMovieService#all` | `GET /api/beemovie` | `/admin/beemovie` |
| `CREDITS` | `SCREEN1` | `BR-CREDITS-01` | `StaticScreenService#credits` | `GET /api/static/credits` | `/credits` |
| `NTRSTIT` | `INTERTEST` | `BR-NTRSTIT-01` | `StaticScreenService#help` | `GET /api/static/help` | `/help` |
| `PARAMETER` | `TESTPARM` | `BR-PARAMETER-01` | `StaticScreenService#parameters` | サービス対応 | テスト用画面 |
| `PRTLSTVOTE`/`PRTLSTCMT`/`PRINTER` | 帳票出力 | `BR-PRTLSTVOTE-01`～`BR-PRTLSTVOTE-03` | `PrintSpoolService#printVoteTicket/printCommentTicket` | `GET /api/print/spool` | 投票・コメント成功画面 |
| `OLDADDVOTE`/`OLDVOTE` | 旧版処理 | `BR-OLDADDVOTE-01`～`BR-OLDADDVOTE-03` | 非移行（仕様資料のみ） | なし | なし |

### BR-ID 個別一覧

上表の範囲表記を展開した全71件は次のとおりである。

| プログラム | BR-ID |
|---|---|
| `ADDVOTE` | `BR-ADDVOTE-01`, `BR-ADDVOTE-02`, `BR-ADDVOTE-03`, `BR-ADDVOTE-04`, `BR-ADDVOTE-05`, `BR-ADDVOTE-06`, `BR-ADDVOTE-07`, `BR-ADDVOTE-08`, `BR-ADDVOTE-09` |
| `ADDGBCMT` | `BR-ADDGBCMT-01`, `BR-ADDGBCMT-02`, `BR-ADDGBCMT-03`, `BR-ADDGBCMT-04`, `BR-ADDGBCMT-05` |
| `READGBCMT` | `BR-READGBCMT-01`, `BR-READGBCMT-02`, `BR-READGBCMT-03`, `BR-READGBCMT-04`, `BR-READGBCMT-05` |
| `LRN400` | `BR-LRN400-01`, `BR-LRN400-02`, `BR-LRN400-03`, `BR-LRN400-04`, `BR-LRN400-05` |
| `LRN400AUT` | `BR-LRN400AUT-01`, `BR-LRN400AUT-02`, `BR-LRN400AUT-03`, `BR-LRN400AUT-04` |
| `ADMLRN400` | `BR-ADMLRN400-01`, `BR-ADMLRN400-02`, `BR-ADMLRN400-03`, `BR-ADMLRN400-04`, `BR-ADMLRN400-05` |
| `EXHBMENU` | `BR-EXHBMENU-01`, `BR-EXHBMENU-02`, `BR-EXHBMENU-03`, `BR-EXHBMENU-04`, `BR-EXHBMENU-05`, `BR-EXHBMENU-06`, `BR-EXHBMENU-07` |
| `ADMVOTERPT` | `BR-ADMVOTERPT-01`, `BR-ADMVOTERPT-02`, `BR-ADMVOTERPT-03`, `BR-ADMVOTERPT-04` |
| `ADMCRTEXHB` | `BR-ADMCRTEXHB-01`, `BR-ADMCRTEXHB-02`, `BR-ADMCRTEXHB-03`, `BR-ADMCRTEXHB-04`, `BR-ADMCRTEXHB-05`, `BR-ADMCRTEXHB-06`, `BR-ADMCRTEXHB-07` |
| `ADMHIDECMT` | `BR-ADMHIDECMT-01`, `BR-ADMHIDECMT-02`, `BR-ADMHIDECMT-03`, `BR-ADMHIDECMT-04` |
| `ADMADDSOFR` | `BR-ADMADDSOFR-01`, `BR-ADMADDSOFR-02` |
| `ADMSETTING` | `BR-ADMSETTING-01`, `BR-ADMSETTING-02`, `BR-ADMSETTING-03` |
| `BEEMOVIE` | `BR-BEEMOVIE-01`, `BR-BEEMOVIE-02` |
| `CREDITS` | `BR-CREDITS-01` |
| `NTRSTIT` | `BR-NTRSTIT-01` |
| `PARAMETER` | `BR-PARAMETER-01` |
| `PRTLSTVOTE` | `BR-PRTLSTVOTE-01`, `BR-PRTLSTVOTE-02`, `BR-PRTLSTVOTE-03` |
| `OLDADDVOTE` | `BR-OLDADDVOTE-01`, `BR-OLDADDVOTE-02`, `BR-OLDADDVOTE-03` |

## DBファイル対応

| RPG PF | Javaテーブル | エンティティ | 主キー |
|---|---|---|---|
| `VOTINGDB` | `VOTINGDB` | `Vote` | `BADGENBR` |
| `AWARDDB` | `AWARDDB` | `Award` | `AWARDID` |
| `EXHBDB` | `EXHBDB` | `Exhibit` | `EXHUSRPRF` |
| `GUESTBKDB` | `GUESTBKDB` | `GuestbookComment` | `CMTID` |
| `LRN400STR` | `LRN400STR` | `Lrn400Page` | `(OWNER, PAGENBR)` |
| `SECOFRS` | `SECOFRS` | `SecurityOfficer` | `USERPROF` |
| `SETTINGS` | `SETTINGS` | `Setting` | `SETTING` |
| `BMOVDB` | `BMOVDB` | `BeeMovieLine` | `LINE` |

## DSPFフォーマットとUI

| DSPFフォーマット | Thymeleafテンプレート | 対応機能 |
|---|---|---|
| `VOTE1`/`VOTEEND`/`ENDOFCON`/`DAVE` | `vote.html` | 投票、完了、投票停止、DAVE |
| `ADDCMT`/`ENDCMT` | `guestbook-add.html` | コメント登録 |
| `READCMT` | `guestbook-read.html` | コメント参照 |
| `MAIN`/`MAINADMIN` | `learn400.html` | LEARN/400 |
| `SFLCTL`/`SFLDATA` | `beemovie.html` | BEEMOVIE |
| `INTERTEST` | `help.html` | `/help` |
| `SCREEN1` | `static.html` | CREDITS |
| `TESTPARM` | API/テスト用画面 | PARAMETER |
| `MENU` | `menu.html` | EXHBMENU |
| `SETUPMAIN`/`ADMSCR` | `admin.html`, `admin-settings.html` | `/admin`, `/admin/settings` |
