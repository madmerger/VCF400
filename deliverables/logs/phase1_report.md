# Phase 1: PUB400 ソース再ビルド・検証 報告

実行日: 2026-10-14 (UTC) / ホスト: pub400.com / ソース: **ASHIBATA1** / オブジェクト・データ: **ASHIBATA2**
他ライブラリには一切書き込んでいない。ASHIBATA2 の既存 PF `ASHIBATA` (VCF400 無関係) は保持。認証情報はシークレット経由のみで、ログ・ファイルに含めていない。

## 1. セーブファイル (記録として 1 回のみ)

| 手順 | 結果 | ログ |
|---|---|---|
| `savf/VCFV1R3.SAVF` を FTP(21, binary, `namefmt 1`) で `//QSYS.LIB/ASHIBATA1.LIB/VCFV1R3.FILE` へ転送 | 成功 5,892,480 bytes (exit 0) | `phase1_02_ftp_savf.log` |
| `DSPSAVF FILE(ASHIBATA1/VCFV1R3)` | SAVLIB / ライブラリ VCF / V4R4M0 / 11,160 レコード / 2024-04-04 20:40:52 | `phase1_03_dspsavf.log` |
| RSTLIB / RSTOBJ | **未実施** (AGENTS.md: 一般権限なし CPD0032)。ソースビルドを正規手順とした | — |

補足: 初回は FTP パスが `/home/ASHIBATA/QSYS.LIB...` と相対解釈され 426 で失敗。URL に `//QSYS.LIB` (絶対パス) を指定して解決 (`pub400_savf.sh`)。

## 2. ソースビルド (1 本の CL / 1 ジョブ)

- `pub400_build.sh`: ソースの SEU 行番号除去 → ISO-8859-1 変換 → `VCF/` 修飾を `ASHIBATA2/` へ置換 (CL スタブ・メニュー) → ASHIBATA1 のソース PF へ転送 → CL `BUILDVCF` (`pub400_BUILDVCF.clle`) を生成し **1 ジョブ**で CHGLIBL + 全作成を実行。
- ビルド順: PF → LF → DSPF/PRTF → MSGF/MNU → RPG (CRTBNDRPG) → CL (CRTBNDCL) → CMD。
- `PRTLSTVOTE.rpgle` の `FVOTINGDB IF E DISK` → `IF E K DISK` はステージング時のみ補正 (リポジトリのソースは無変更)。
- OLDVOTE / OLDADDVOTE / PRINTER / BEEMOVIE はスコープ外のためビルド対象から除外。

### 結果 (コンパイルリスト `pub400_listings/*.txt` から抽出)

| 種別 | 結果 |
|---|---|
| RPG 18 本 | **全件 `00 highest severity`**: NTRSTIT, PARAMETER, PRTLSTVOTE, PRTLSTCMT, CREDITS, ADDVOTE, ADDGBCMT, READGBCMT, LRN400, LRN400AUT, EXHBMENU, ADMADDSOFR, ADMCRTEXHB, ADMHIDECMT, ADMLRN400, ADMOFRLIST, ADMSETTING, ADMVOTERPT |
| CL 5 本 | VOTESTUB, ADDGBSTUB, READGBSTUB, VCFSTUB, LRN400STUB 作成 |
| DSPF/PRTF/MNU | 全件作成 (VCFMAIN, MAIN, ADMMAIN メニュー含む) |
| PF / CMD | 全件作成 (CHKOBJ で全期待オブジェクトを 1 回の SSH で検証) |
| ASHIBATA2 インベントリ | 62 オブジェクト (既存 PF `ASHIBATA` を含む) |

## 3. ベースラインデータ

- `pub400_seed.sql` (EXHBDB / AWARDDB / SETTINGS / SECOFRS / LRN400STR / GUESTBKDB / VOTINGDB) を投入 (`phase1_05b_seed.log`)。
- テスト前後の再シードは `tests/pub400_db.py reset` (VOTINGDB/GUESTBKDB/SETTINGS をベースラインに戻す)。
- 注意: PUB400 の SSH(2222) は短時間に接続が集中すると `kex_exchange_identification: Connection closed` で一時的に拒否される。数分待って再試行で回復。
