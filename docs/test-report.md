# フェーズ5 テスト実行結果レポート

## 1. 実行概要

| 項目 | 内容 |
|---|---|
| 実行日時 | 2026-09-04T03:31:25Z 開始、03:31:33Z 終了 |
| 実行コマンド | `cd java && mvn -q verify` |
| Surefire HTML生成 | `cd java && mvn -q surefire-report:report -DskipTests` |
| Java | OpenJDK 17.0.13 |
| Maven | Apache Maven 3.9.9 |
| Spring Boot | 3.3.5 |

`mvn verify` は、テスト実行と JaCoCo の `verify` フェーズレポート生成まで含めて成功した。Surefireレポートは、生成済みのテスト結果を使用して `mvn surefire-report:report -DskipTests` で生成した。最終実行では全テストが成功した。

## 2. テスト結果

| 項目 | 件数 |
|---|---:|
| 総テスト件数 | 157 |
| 成功 | 157 |
| 失敗 | 0 |
| エラー | 0 |
| スキップ | 0 |

今回の `mvn verify` で全157件が成功した。日本語 UI の MessageSource と RPG メッセージ変換について、UI の表示内容および重複投票メッセージを追加検証した。

### クラス別件数

`@Nested` の `Normal`、`Error`、`Boundary` に含まれるテストを、RPGプログラム別の外側クラスへ集計した。

| テストクラス | Normal | Error | Boundary | 合計 |
|---|---:|---:|---:|---:|
| AddVoteServiceTest | 3 | 9 | 2 | 14 |
| AddGuestbookCommentServiceTest | 4 | 3 | 2 | 9 |
| ReadGuestbookCommentServiceTest | 4 | 3 | 2 | 9 |
| Learn400ServiceTest | 7 | 1 | 3 | 11 |
| Learn400AutoServiceTest | 2 | 1 | 1 | 4 |
| AdminLearn400ServiceTest | 3 | 1 | 2 | 6 |
| ExhibitMenuServiceTest | 9 | 4 | 1 | 14 |
| AdminVoteReportServiceTest | 3 | 1 | 1 | 5 |
| AdminExhibitServiceTest | 5 | 5 | 2 | 12 |
| AdminHideCommentServiceTest | 3 | 2 | 1 | 6 |
| AdminSecurityOfficerServiceTest | 2 | 2 | 1 | 5 |
| AdminSettingServiceTest | 3 | 1 | 2 | 6 |
| StaticScreenServiceTest | 3 | 1 | 2 | 6 |
| BeeMovieServiceTest | 1 | 1 | 1 | 3 |
| PrintSpoolServiceTest | 2 | 1 | 1 | 4 |
| DdsFieldTest | 1 | 1 | 2 | 4 |
| VoteApiTest | 1 | 2 | 1 | 4 |
| AdminApiTest | 2 | 1 | 1 | 4 |
| UiPagesTest | 24 | 3 | 1 | 28 |
| UiMessageTranslatorTest | 3 | 0 | 0 | 3 |
| **合計** | **85** | **43** | **29** | **157** |

全テストは1ケース1メソッドで作成し、サービス層はRPGプログラム別クラス、Web層はAPI/UI別クラスに分割した。サービス層のテストは `@DataJpaTest` と `@Import` によりH2実DBへ接続し、repositoryの実分岐を実行している。

## 3. JaCoCoカバレッジ

JaCoCoの全体値は、命令数ではなくラインおよびブランチを記載する。

| 範囲 | ライン | ブランチ |
|---|---:|---:|
| 全体 | 887/1044 (84.96%) | 200/289 (69.20%) |

### パッケージ別

| パッケージ | ライン | ブランチ |
|---|---:|---:|
| `com.vcf400.service` | 577/611 (94.44%) | 181/216 (83.80%) |
| `com.vcf400.domain` | 111/118 (94.07%) | 3/6 (50.00%) |
| `com.vcf400.web.ui` | 152/223 (68.16%) | 16/61 (26.23%) |
| `com.vcf400.web.api` | 28/71 (39.44%) | 0/6 (0.00%) |
| `com.vcf400.print` | 18/18 (100.00%) | 0/0 (対象なし) |
| `com.vcf400` | 1/3 (33.33%) | 0/0 (対象なし) |

### 未達箇所と理由

- `com.vcf400.web.ui.Vcf400UiController` は、画面ごとのGET/POST分岐が多く、今回の代表的なHTTP 200確認と投票エラー表示を中心に検証したため、未使用の管理画面POST分岐などが残っている。
- `com.vcf400.web.api.Vcf400ApiController` は、今回の代表API（投票、管理出展、投票集計）の検証範囲外となった管理API分岐が残っている。APIのサービス層ロジック自体はサービス層テストで検証している。
- `Learn400Service`、`AdminExhibitService`、`AdminLearn400Service` などは、存在しないページ・削除確認・状態遷移の組み合わせに未実行分岐が残るが、主要なRPG分岐は個別テストで確認している。
- RPG仕様で旧版・非移行とされる `OLDADDVOTE`、`OLDVOTE` 等はJava実装対象外であり、対応コードおよびカバレッジ対象に含めていない。
- デバッグ専用の旧RPG定数・処理も移行対象外である。

カバレッジ未達は、未実装仕様をテストから隠した結果ではなく、旧版非移行コードおよび今回の代表シナリオ外のUI/API分岐によるものである。

## 4. 日本語 UI スモークテスト

`java -jar target/vcf400-java-0.0.1-SNAPSHOT.jar` を起動し、`Started Vcf400Application` を確認した。次の主要画面はすべて HTTP 200 で、本文の日本語表示も確認した。

| パス | 確認内容 |
|---|---|
| `/` | `VCF/400 メインメニュー`、日本語メニュー |
| `/learn400?owner=LRN400STR` | `ページ 0001`、LEARN/400本文 |
| `/help?next=/vote?profile=ASHIBATA` | `NTRSTIT`、日本語続行案内 |
| `/vote?profile=ASHIBATA` | 日本語投票見出し・入力ラベル |
| `/guestbook/add?profile=ASHIBATA` | 日本語入力ラベル |
| `/guestbook/read?profile=ASHIBATA` | 日本語見出し・件数表示 |
| `/signon` | `VCF/400 サインオン` |
| `/admin` | 管理メニュー |
| `/admin/db` | DBVERIFY の日本語見出し |

固定文言を対象に `Must enter`、`Press ENTER`、`Currently hosting`、`Submit`、`Exit`、`Cancel`、`Instructions` の残存がないことを確認した。LEARN/400本文や Bee Movie 台詞などの動的原文は対象外とした。確認ログは `/home/ubuntu/vcf400-japanese-ui-smoke.txt` に保存した。

## 5. レポートへのリンク

- [Surefire HTMLレポート](test-reports/surefire/surefire-report.html)
- [JaCoCo HTMLレポート](test-reports/jacoco/index.html)
- 保存済みSurefire XML: [`docs/test-reports/surefire/`](test-reports/surefire/)
- 保存済みJaCoCo一式: [`docs/test-reports/jacoco/`](test-reports/jacoco/)

## 6. PUB400 E2Eシナリオのcurl確認

起動したSpring Bootアプリケーションに対して、シナリオ§1〜§6の代表操作をcurlで確認した。確認ログは `/home/ubuntu/vcf400-pub400-e2e-final.txt` に保存した。

| 節 | 確認内容 | 結果 |
|---|---|---|
| §1 | VCFMAINの4プロフィール・6メニュー、およびSIGN-ON | 成功 |
| §2 | LEARN/400 `Page 0001` → `0002` → `0003`、ページ本文、END | 成功 |
| §3 | 投票成功、同一バッジ重複、NOVOTE拒否、VOTE1のreadonly | 成功 |
| §4 | ゲストブック投稿ID=1、本文参照、展示名・件数表示 | 成功 |
| §5 | EXHBMENUのASHIBATA表示、投票集計 | 成功 |
| §6 | `/admin/db` のVOTINGDB/GUESTBKDB表と登録行 | 成功 |
