# フェーズ5 テスト実行結果レポート

## 1. 実行概要

| 項目 | 内容 |
|---|---|
| 実行日時 | 2026-09-02T16:38:03Z 開始、16:38:11Z 終了 |
| 実行コマンド | `cd java && mvn verify` |
| Surefire HTML生成 | `cd java && mvn surefire-report:report -DskipTests` |
| Java | OpenJDK 17.0.13 |
| Maven | Apache Maven 3.9.9 |
| Spring Boot | 3.3.5 |

`mvn verify` は、テスト実行と JaCoCo の `verify` フェーズレポート生成まで含めて成功した。Surefireレポートは、生成済みのテスト結果を使用して `mvn surefire-report:report -DskipTests` で生成した。

## 2. テスト結果

| 項目 | 件数 |
|---|---:|
| 総テスト件数 | 147 |
| 成功 | 147 |
| 失敗 | 0 |
| エラー | 0 |
| スキップ | 0 |

今回の `mvn verify` で全147件が成功した。失敗がなかったため、原因分析および修正対象となるテスト失敗はない。

### クラス別件数

`@Nested` の `Normal`、`Error`、`Boundary` に含まれるテストを、RPGプログラム別の外側クラスへ集計した。

| テストクラス | Normal | Error | Boundary | 合計 |
|---|---:|---:|---:|---:|
| AddVoteServiceTest | 3 | 9 | 2 | 14 |
| AddGuestbookCommentServiceTest | 4 | 3 | 2 | 9 |
| ReadGuestbookCommentServiceTest | 4 | 3 | 2 | 9 |
| Learn400ServiceTest | 6 | 1 | 3 | 10 |
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
| UiPagesTest | 19 | 2 | 1 | 22 |
| **合計** | **76** | **42** | **29** | **147** |

全テストは1ケース1メソッドで作成し、サービス層はRPGプログラム別クラス、Web層はAPI/UI別クラスに分割した。サービス層のテストは `@DataJpaTest` と `@Import` によりH2実DBへ接続し、repositoryの実分岐を実行している。

## 3. JaCoCoカバレッジ

JaCoCoの全体値は、命令数ではなくラインおよびブランチを記載する。

| 範囲 | ライン | ブランチ |
|---|---:|---:|
| 全体 | 821/985 (83.35%) | 191/283 (67.49%) |

### パッケージ別

| パッケージ | ライン | ブランチ |
|---|---:|---:|
| `com.vcf400.service` | 572/609 (93.92%) | 179/216 (82.87%) |
| `com.vcf400.domain` | 111/118 (94.07%) | 3/6 (50.00%) |
| `com.vcf400.web.ui` | 91/166 (54.82%) | 9/55 (16.36%) |
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

## 4. レポートへのリンク

- [Surefire HTMLレポート](test-reports/surefire/surefire-report.html)
- [JaCoCo HTMLレポート](test-reports/jacoco/index.html)
- 保存済みSurefire XML: [`docs/test-reports/surefire/`](test-reports/surefire/)
- 保存済みJaCoCo一式: [`docs/test-reports/jacoco/`](test-reports/jacoco/)
