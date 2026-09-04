# データベース詳細仕様

## 1. DDS 型

| DDS 型 | 意味 | Java/H2 マッピング |
|---|---|---|
| `A` | 文字列（固定長） | `VARCHAR(n)`（入力互換を優先） |
| `B` | 2進数（binary numeric） | 桁に応じて `INTEGER` または `SMALLINT` |
| `D` | 表示用十進数（packed/display decimal） | `INTEGER`（例: `4D0`） |
| `Y` | 2進数フィールドの画面定義 | `INTEGER`（画面入力では数値） |

`nA` は英数字固定長 `n` 桁、`nB 0`/`nD 0` は小数桁 0 の整数である。RPG の `MOVEL` による余白・切り詰めは Java の境界変換で明示する。

## 2. スキーマ

| PF | レコード | フィールド | DDS | H2 型 | キー/制約 |
|---|---|---|---|---|---|
| AWARDDB | AWARDRCD | AWARDID | 3B 0 | INTEGER | PK, UNIQUE |
|  |  | AWARDTITLE | 100A | VARCHAR(100) |  |
|  |  | AWARDDESC | 1000A | VARCHAR(1000) |  |
| BMOVDB | BMOVREC | SPEAKER | 9A | VARCHAR(9) |  |
|  |  | LINE | 50A | VARCHAR(50) | PK, UNIQUE |
| EXHBDB | EXHBREC | EXHBDBID | 3B 0 | INTEGER | 0固定 |
|  |  | EXHUSRPRF | 9A | VARCHAR(9) | PK, UNIQUE |
|  |  | EXHBITOR | 20A | VARCHAR(20) |  |
|  |  | EXHBCITY | 20A | VARCHAR(20) |  |
|  |  | EXHBSTATE | 2A | VARCHAR(2) |  |
|  |  | EXHBTITLE | 50A | VARCHAR(50) |  |
|  |  | EXHBDESC | 1000A | VARCHAR(1000) |  |
|  |  | ELIGIBLE | 1B 0 | INTEGER | 0/1 |
|  |  | ENLRN400 | 1B 0 | INTEGER | 0/1 |
| GUESTBKDB | GUESTBKRCD | CMTID | 4B 0 | INTEGER | PK, UNIQUE |
|  |  | VISIBLE | 1A | VARCHAR(1) | Y/N |
|  |  | EXHBID | 9A | VARCHAR(9) |  |
|  |  | GUESTNAME | 20A | VARCHAR(20) |  |
|  |  | GUESTCMT | 200A | VARCHAR(200) |  |
| LRN400STR | LRN400RCD | PAGENBR | 4B 0 | INTEGER | PK, UNIQUE |
|  |  | CONTENT | 1500A | VARCHAR(1500) |  |
|  |  | EXTRA | 9A | VARCHAR(9) |  |
| SECOFRS | USRPROFR | USERPROF | 9A | VARCHAR(9) | PK, UNIQUE |
| SETTINGS | SETTINGSR | SETTING | 9A | VARCHAR(9) | PK, UNIQUE |
|  |  | VALUE | 9A | VARCHAR(9) |  |
| VOTINGDB | VOTINGREC | BADGENBR | 4B 0 | INTEGER | PK, UNIQUE |
|  |  | AWARDNBR | 3B 0 | INTEGER |  |
|  |  | EXHBNBR | 9A | VARCHAR(9) |  |

## 3. キーとアクセス方針

- `VOTINGDB` は `BADGENBR` 一意で、1バッジ1票を表現する。
- `EXHBDB` は `EXHUSRPRF`、`AWARDDB` は `AWARDID`、`GUESTBKDB` は `CMTID`、`LRN400STR` は `PAGENBR`、`SETTINGS` は `SETTING`、`SECOFRS` は `USERPROF` がキーである。
- RPG の `SETLL` はキー位置を合わせるだけで完全一致ではない。Java/H2 では業務ルールに必要な場合、主キーの完全一致 `findById` と明示的な順序検索を分ける。
- `ADDGBCMT` は EOF まで `READ` して最後の `CMTID+1` を採番する。Java では同時実行時の重複を避ける採番方式をフェーズ2で確定する。

## 4. 初期データの前提

Java では `SETTINGS` に `ALWVOTE=Y`、`PASSWORD=VCF400`、`SECOFRS` に `MM2024` をシードする。`AWARDDB` は 001=`Best in Show`、002=`Ed Fair` とする。`EXHBDB` は少なくとも `GERTIE`（適格・LEARN/400有効）、`NOVOTE`（不適格）、その他1件を用意し、`LRN400STR` と `BMOVDB` に表示確認用データを入れる。

## 5. RPG 既知の問題

- `ADDVOTE` と `READGBCMT` の `SETLL+READ` は対象が存在しない場合に次レコードを処理し得る。
- `EXHBMENU` は設定ファイルの先頭レコードを終了パスワードとして読むため、キー順では `ALWVOTE` の値になる。
- `EXHBDBID` は RPG で設定されない。
- `SECOFRS` は定義されているが、認可チェックサブルーチンは空である。
