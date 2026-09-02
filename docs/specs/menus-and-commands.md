# メニュー・コマンド・CL スタブ仕様

## 1. メニュー

### VCFMAIN

| オプション | 表示 | 呼出し先 |
|---:|---|---|
| 1 | `Learn AS/400 Navigation` | `LRN400`（`VCFMAINQQ` は直接 CALL） |
| 2 | `IBM Midrange History` | `VCFEXPO/HISTMNU`（本リポジトリ外） |
| 3 | `About This System` | `VCFEXPO/ABOUT150`（本リポジトリ外） |
| 4 | `About Gertie the System/34` | `VCFEXPO/ABTGRTIE`（本リポジトリ外） |
| 5〜10 | `Start OfficeVision/400`、`Start a Calendar`、`Start the Clock`、`Play Snake`、`Play Yahtzee`、`Play PacMan` | メニュー DDS 上の案内。呼出し実装は本リポジトリ外または未定義 |
| 11 | `Nominate Exhibit for Award` | `VOTESTUB` |
| 12 | `Sign Exhibit Guestbook` | `ADDGBSTUB` |
| 13 | `Read a Guestbook Comment` | `READGBSTUB` |
| 80 | `Sign Off the System` | `signoff` |
| 90 | `Start Admin Menu` | 管理メニューへの遷移 |

### MAIN / ADMMAIN

`MAINQQ` は 0001〜0004 を `VCFEXPO/NAV001`、`VCFEXPO/HISTMNU`、`VCFEXPO/ABOUT150`、`VCFEXPO/ABTGRTIE` へ `GO` し、0080 は `signoff`。`MAIN` のその他表示オプションは本リポジトリ外のメニュー機能である。

`ADMMAIN` の表示は次のとおりである。RPG 呼出し実装は本リポジトリにないため、呼出し先は前提として整理する。

| オプション | 表示 | 呼出し先（前提） |
|---:|---|---|
| 1 | `Create New Exhibit` | `ADMCRTEXHB`（`EXHIBIT='NONE'`） |
| 2 | `Edit Existing Exhibit` | `ADMCRTEXHB` |
| 3 | `Moderate Comment` | `ADMHIDECMT` |
| 4 | `End All Voting` | `ADMSETTING` で `ALWVOTE` を変更する運用を想定 |
| 5 | `View Voting Statistics` | `ADMVOTERPT` |
| 6〜10 | 表示なし | 未実装 |

## 2. カスタムコマンド

| コマンド | パラメータ | 呼出し先 |
|---|---|---|
| `STRCMTEDT` | `CMTID` `*CHAR` 4、必須、`0001-9999` | `ADMHIDECMT` |
| `STREXHB` | `EXHBNAME` `*NAME` 9、必須 | `EXHBMENU` |
| `STREXHBEDT` | `PASSWORD` `*CHAR` 9（必須指定なしの原文コメントあり）、`EXHBNAME` `*NAME` 9、必須 | `ADMCRTEXHB`（パラメータ連携は前提） |
| `STRVOTERPT` | `EXHBNAME` `*NAME` 9、必須 | `ADMVOTERPT` |

## 3. CL スタブ

| スタブ | 処理 |
|---|---|
| `ADDGBSTUB` | `PGM`、`RTVJOBA CURUSER(&PROFILE)`、`CALL VCF/NTRSTIT`、`CALL VCF/ADDGBCMT PARM(&PROFILE)`、`ENDPGM` |
| `READGBSTUB` | `PGM`、現在ユーザ取得、`NTRSTIT` 表示、`READGBCMT` 呼出し |
| `VOTESTUB` | `PGM`、現在ユーザ取得、`NTRSTIT` 表示、`ADDVOTE` 呼出し |
| `LRN400STUB` | 現在ユーザ取得、`OVRDBF FILE(LRN400STR) TOFILE(VCF/&PROFILE)`、`LRN400` 呼出し、`DLTOVR` |
| `VCFSTUB` | 現在ユーザ取得、`EXHBMENU` 呼出し |

`LRN400STUB` の `TOFILE(VCF/&PROFILE)` は、出展者プロファイル名と同名の物理ファイルがあることを前提にした IBM i の切替である。Java では `LRN400STR.owner` を追加して同一テーブル内で再現する。
