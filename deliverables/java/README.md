# VCF/400 Web (Java 版)

VCF/400 (IBM i / RPG IV) の Java マイグレーション。Spring Boot 3 + Thymeleaf + H2 (DB2 モード)。
仕様は `../spec/VCF400_spec.md` を正とし、画面構造は同書の「元画面構造の台帳 (L-xx)」に 1 対 1 で対応する。

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
- `VCF_DB_API` … クロス検証用 DB API の有効化 (既定 `false`)。それ以外では無効のままにする。
  - `GET /api/db/settings` は `ADMPSWRD` の値を `********` にマスクして返す。
  - `PUT /api/db/settings/{name}` は `ALWVOTE` / `ADMPSWRD` のみ受け付ける (それ以外は 400)。
- `VCF_SIGNON` … サインオンフォーム (IBM i SIGNON 相当、パスワードなし) の有効化 (既定 `VCF_DB_API` の値)。
  無効時は `POST /signon` が 403、メニューにフォームを表示しない。プロファイルは `VCF_PROFILE` とキオスクフローのみから決まる。
- `VCF_H2_CONSOLE` … H2 コンソール `/h2` の有効化 (既定 `false`)。開発時だけ必要に応じて `true` にする。
- `VCF_COOKIE_SECURE` … セッション Cookie の `Secure` 属性 (既定 `false`)。HTTPS 配下では `true` にする。
- `VCF_SEED_ADMPSWRD` … H2 初期シードの管理者パスワード (既定 = 空)。空のままだと ADMPSWRD が空文字でシードされ、キオスク終了 (隠しオプション 7) はどのパスワードでも通らない (警告ログを出力)。実運用では必ず設定する。

クロス検証の起動:

```sh
VCF_DB_API=true VCF_SEED_ADMPSWRD=VCF2024 mvn spring-boot:run
# (VCF_DB_API=true でサインオンフォームも有効。run_java.js のリセットが ADMPSWRD=VCF2024 を再設定する)
```

CSRF 対策: synchronizer token。セッション属性に乱数トークン (SecureRandom 32B) を遅延生成し
(@ControllerAdvice の `csrfToken` モデル属性)、全 POST フォームに hidden `_csrf` を埋め込む。
POST/PUT/DELETE は `_csrf` パラメータまたは `X-CSRF-Token` ヘッダがセッショントークンと一致する
要求だけ通す (定数時間比較、セッション/トークンなしは 403)。`/api/db/` はトークン対象外だが、ブラウザ起点の変更を防ぐため Origin / Sec-Fetch-Site ヘッダ付きの
変更リクエストは 403 とし、変更メソッドは `Content-Type: application/json` を要求する
(`/h2` (既定 OFF) も対象外)。SameSite=strict Cookie と併せた多層防御。

初期化とシード:

- `schema.sql` は H2 (`jdbc:h2:`) のときだけ、起動時の `DataSeeder` が実行する (全て `CREATE TABLE IF NOT EXISTS` のため再起動でも安全)。
- 旧バージョンで作成済みのファイル DB (LRNFILE 列なし LRN400STR) は起動時に `DataSeeder` が自動で
  `ALTER TABLE` して (LRNFILE, PAGENBR) PK へアップグレードする。既存行は `LRNFILE='LRN400STR'` で保持される。
  移行途中で停止した場合 (列追加済み・PK 未変更) も次回起動時に PK 変更から再開する。
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

展示ごとの LEARN/400 ページ: レガシーではキオスクのオプション 2 が `OVRDBF FILE(LRN400STR) TOFILE(VCF/&PROFILE)` で展示 ID と同名のページファイルに切り替える (VCFMAIN 1 は共通 LRN400STR)。
Java 版では LRN400STR テーブルの `LRNFILE` 列 (既定 `'LRN400STR'`、PK = LRNFILE+PAGENBR) で同じことを表現する。キオスク起動時はセッションに展示 ID を保持して `LRNFILE=展示ID` のページを読む。展示ファイルにページが 1 枚もない場合は共通 `LRN400STR` にフォールバックして警告ログを出す (レガシーは存在しないファイルで異常終了するため、ここは仕様上の差異)。
