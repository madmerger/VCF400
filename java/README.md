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
- `VCF_DB_URL` … JDBC URL。既定は `./data/vcf400` の H2 ファイル DB (DB2 モード)。DB2 for i へ向ける場合は
  `jdbc:as400://host/ASHIBATA2` 等を指定し、jt400 ドライバを依存に追加する (スキーマは DDS と同一列名)。
- `VCF_PROFILE` … 既定のサインオンユーザー (LAUNCH)。`MM2024` で共用端末モード。

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
