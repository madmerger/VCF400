# VCF/400 Web (Java 版)

VCF/400 (IBM i / RPG IV) の Java マイグレーション。Spring Boot 3 + Thymeleaf + H2 (DB2 モード)。
仕様は `../deliverables/spec/VCF400_spec.md` を正とし、画面構造は同書の「元画面構造の台帳 (L-xx)」に 1 対 1 で対応する。

## 起動

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 17)   # Java 17
mvn spring-boot:run                                  # http://localhost:8080/menu
mvn test                                             # 業務ルール / 画面遷移テスト
```

- `VCF_PORT` … ポート (既定 8080)
- `VCF_DB_URL` … JDBC URL。既定は `./data/vcf400` の H2 ファイル DB (DB2 モード)。本 PR の対応 DB は H2 (DB2 モード) のみ。
  DB2 for i へ向けるには jt400 ドライバの依存追加と接続確認が別途必要 (未対応)。
- `VCF_PROFILE` … 既定のサインオンユーザー (LAUNCH)。`MM2024` で共用端末モード。
- `VCF_DB_API` … クロス検証用 DB API の有効化 (既定 `false`)。クロス検証時は `VCF_DB_API=true mvn spring-boot:run` のように起動する。それ以外では無効のままにする。
- `VCF_H2_CONSOLE` … H2 コンソール `/h2` の有効化 (既定 `false`)。開発時だけ必要に応じて `true` にする。
- `VCF_COOKIE_SECURE` … セッション Cookie の `Secure` 属性 (既定 `false`)。HTTPS 配下では `true` にする。
- `VCF_SEED_ADMPSWRD` … H2 初期シードの管理者パスワード (既定 `VCF2024`)。PUB400 検証データと一致する既定値であり、実運用では必ず上書きする。

初期化とシード:

- `schema.sql` は H2 (`jdbc:h2:`) のときだけ Spring の embedded 初期化で実行される。
- H2 では `SETTINGS` が空の場合に限り、起動時の `DataSeeder` が `seed.sql` を 1 回実行する。既存のファイル DB を再起動してもデータを再投入しない。
- シード後の管理者パスワード `ADMPSWRD` は SETTINGS のキーで参照して更新する (先頭レコードには依存しない)。
- DB2 for i (`jdbc:as400:`) ではスキーマ・シード初期化を行わず、既存の DDS テーブルを使用する。
- DB API はクロス検証専用で、ループバック接続からのみ利用できる。

## 画面と URL

| 元プログラム / 画面 | URL |
|---|---|
| VCFMAIN メニュー | `/menu` |
| NTRSTIT (How to Navigate) | `/navigate?next=...` |
| ADDVOTE (VOTESCR/VOTE1, VOTEEND, ENDOFCON) | `/vote` |
| ADDGBCMT (GUESTBKSCR/ADDCMT, ENDCMT) | `/guestbook/add` |
| READGBCMT (GUESTBKSCR/READCMT) | `/guestbook/read` |
| LRN400 (LRN400SCR/MAIN) | `/learn` |
| EXHBMENU (STREXHB EXHBNAME(x)) | `/kiosk/{x}` / 隠しオプション 7 → `/kiosk/{x}/exit` |
| クロス検証用 DB 状態 API | `/api/db/votes`, `/api/db/comments`, `/api/db/settings` |

ファンクションキーは物理キー (F3/F5/F8/F12/ENTER) とボタン (「送信 (F5)」等) の両方で操作できる。

## UI 日本語化と英語原文の対応

画面の主表示は日本語とし、台帳上の画面タイトルは英語原文を保持して日本語副題を併記する。エラー行 `#errline` はクロス検証用に英語原文のみを表示し、日本語訳は `errline-ja` に表示する。ボタンには対応する F キーを併記し、DB 由来の値は翻訳しない。サインオンボタンの `aria-label="Sign on"` はクロス検証ランナー互換のため保持する。

| ID | 英語原文 | 日本語 |
|---|---|---|
| M-02 | Must enter badge number | バッジ番号を入力してください |
| M-03 | Must enter Exhibit ID | 展示 ID を入力してください |
| M-04 | Must enter Award ID | アワード ID を入力してください |
| M-05 | You have already voted. | すでに投票済みです。 |
| M-07 | Exhibit ineligible for award | この展示はアワードの対象外です |
| M-08 | Exhibit does not exist | 展示が存在しません |
| M-09 | Award does not exist. | アワードが存在しません。 |
| M-10 | Must enter Exhibit ID | 展示 ID を入力してください |
| M-11 | Must enter your name | お名前を入力してください |
| M-12 | Must enter a comment | コメントを入力してください |
| M-13 | Must enter CommentID | コメント ID を入力してください |
| M-14 | Name Hidden | 名前は非表示 |
| M-14 | This comment hidden by an admin - offensive content. | このコメントは管理者により非表示にされています (不適切な内容)。 |
| M-15 | This comment is not part of this guestbook. | このコメントはこのゲストブックのものではありません。 |

分類依存の表示は、`THANK YOU FOR VOTING!` → 「投票ありがとうございました!」、`THANKS FOR COMMENTING!` → 「コメントありがとうございました!」、`SORRY!` → 「申し訳ありません」。画面タイトルの英語原文も台帳どおり保持し、日本語副題を別要素に表示する。

LEARN/400 の `CONTENT='CALL'` は、RPG の次ページへ進む流れを維持しつつ、Java 版では呼出先プログラムを実行せず警告ログを出して次ページを表示する。`CONTENT='JUMP'` は従来どおり `EXTRA` のページへ移動する。
