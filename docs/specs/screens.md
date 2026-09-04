# 画面仕様

## 1. DSPF レコードフォーマット一覧

| DSPF | レコードフォーマット | 主な項目 | ファンクションキー |
|---|---|---|---|
| `VOTESCR` | `VOTE1` | `INPUTBADGE` 4D0、`INEXHB` 9A、`INPUTAWARD` 3D0、`ERRLINE` 45A | F5 Submit、F12 Cancel |
|  | `VOTEEND` | 投票完了固定文言 | Enter |
|  | `DAVE` | イースターエッグ固定文言 | Enter |
|  | `NOVOTEALWD` | 不適格展示案内 | Enter |
|  | `ENDOFCON` | 投票期間終了案内 | Enter |
| `GUESTBKSCR` | `ADDCMT` | `INNAME` 16A、`INID` 9A、`INCMT` 200A、`ERRLINE` 21A | F5 Submit、F12 Cancel |
|  | `ENDCMT` | 完了案内 | Enter |
|  | `READCMT` | `INCMTID` 4Y0、`OUTNAME` 11A、`OUTCMT` 200A、`OUTTITLE` 50A、`WTFCMTNUM` 4、`ERRLINE` 20A | F5 Submit、F12 Cancel |
|  | `HIDECMT` | `HIDECMTID` 4A、`OUTNAME` 11A、`OUTCMT` 200A、`CURRHIDE` 1A、`INHIDEYN` 1A、`ERRLINE` 20A | F5 Submit、F12 Cancel |
| `LRN400SCR` | `MAIN` | `OUTPAGENBR` 4A、`OUTCONTENT` 1500A | F3 Exit、F5 Fwd、F8 Back |
|  | `MAINADMIN` | `OUTPAGENBR` 4A、`INCONTENT` 1500A、`INEXTRA` 9A | F3 Exit、F5 Fwd、F8 Back、F10 Save |
| `LRNAUTO` | `MAIN` | `OUTPAGENBR` 4A、`OUTCONTENT` 1500A | F3 Exit、F5 Fwd、F8 Back |
|  | `MAINADMIN` | `OUTPAGENBR` 4A、`INCONTENT` 1500A、`INEXTRA` 9A | F3 Exit、F5 Fwd、F8 Back、F10 Save |
| `EXHBMENUSC` | `MENU` | `OUTTITLE` 75A、`OUTNAME` 25A、`OUTCITY` 15A、`OUTSTATE` 2A、`OUTDESC` 1000A、`INOPT` 1Y0 | Enter |
|  | `ADMCREATE` | `INUSRPRF` 9A、`INTITLE` 60A、`INNAME` 20A、`INCITY` 15A、`INSTATE` 2A、`INDESC` 1000A、`INELIGIBLE` 1A、`INLRN400EN` 1、`ERRLINE` 30A | Enter、F5 Delete、F12 Cancel |
|  | `ADMPSWRD` | `INPWD` 9A | Enter |
|  | `ADMDELETE`/`ADMDELETE2` | `DLTEXHNAM` 9A | Enter、F12 Cancel |
| `ADMVOTERES` | `VOTERES` | `INUSRPRF` 9A、`OUTTITLE` 50A、`OUTEFA` 4A、`OUTBIS` 4A、`OUTVOTEN` 4A、`ERRLINE` 40A | F3 Exit、Enter |
| `ADMSCR` | `ADDADM` | `INADMNAME` 9A | Enter |
|  | `LISTADMRCD` | 固定プレースホルダ文言 | Enter |
| `SETUPSCR` | `SETUPMAIN` | `INPWD` 9A、`INALWVOTE` 1 | Enter |
| `BMOVSCR` | `SFLDATA`/`SFLCTL` | `SPEAKER` 9A、`LINE` 50A | Roll Up/Down、F3 Exit |
| `CREDITSSCR` | `SCREEN1` | 固定クレジット文言 | Enter |
| `INTERSCR` | `INTERTEST` | 固定操作ヘルプ文言 | Enter |
| `TESTSUITE` | `TESTPARM` | `PARMOUT` 20、`PARMOUT2` 20、`SOMETHING` 5A | Enter |

## 2. 表示属性

`VOTESCR` の `*IN40`〜`*IN42`、`GUESTBKSCR` の `*IN40`〜`*IN43` は入力エラーの反転表示に用いる。`*IN70` は `DSPATR(PR)` による保護を表す前提である。Java 画面では、これらの指標をエラー項目の強調・入力不可として表現する。

## 3. 画面遷移上の注意

画面遷移は各プログラム仕様書に Mermaid で記載する。`EXFMT` の直後に入力指標を評価するため、RPG では F5 の扱いとエラー再表示の有無がプログラムごとに異なる。`READGBCMT` は検索結果を設定した後に再度 `EXFMT` しないこと、`ADDGBCMT` は投稿後 `ENDCMT` を表示することに注意する。
