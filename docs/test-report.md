# フェーズ5 テスト実行結果レポート

## 1. 実行概要

| 項目 | 内容 |
|---|---|
| 実行日時 | 2026-09-02T23:45:25Z 開始、23:45:33Z 終了 |
| 実行コマンド | `cd java && mvn verify` |
| Surefire HTML生成 | `cd java && mvn surefire-report:report -DskipTests` |
| Java | OpenJDK 17.0.13 |
| Maven | Apache Maven 3.9.9 |
| Spring Boot | 3.3.5 |

`mvn verify` は、テスト実行と JaCoCo の `verify` フェーズレポート生成まで含めて成功した。Surefireレポートは、生成済みのテスト結果を使用して `mvn surefire-report:report -DskipTests` で生成した。最終実行では全テストが成功した。

## 2. テスト結果

| 項目 | 件数 |
|---|---:|
| 総テスト件数 | 151 |
| 成功 | 151 |
| 失敗 | 0 |
| エラー | 0 |
| スキップ | 0 |

今回の `mvn verify` で全151件が成功した。今回、LEARN/400のページ番号・本文、ゲストブックのゼロ埋め件数、および `/admin/db` と `/signon` のシナリオ検証を追加した。

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
| UiPagesTest | 22 | 2 | 1 | 25 |
| **合計** | **80** | **42** | **29** | **151** |

全テストは1ケース1メソッドで作成し、サービス層はRPGプログラム別クラス、Web層はAPI/UI別クラスに分割した。サービス層のテストは `@DataJpaTest` と `@Import` によりH2実DBへ接続し、repositoryの実分岐を実行している。

## 3. JaCoCoカバレッジ

JaCoCoの全体値は、命令数ではなくラインおよびブランチを記載する。

| 範囲 | ライン | ブランチ |
|---|---:|---:|
| 全体 | 836/998 (83.77%) | 196/285 (68.77%) |

### パッケージ別

| パッケージ | ライン | ブランチ |
|---|---:|---:|
| `com.vcf400.service` | 577/611 (94.44%) | 181/216 (83.80%) |
| `com.vcf400.domain` | 111/118 (94.07%) | 3/6 (50.00%) |
| `com.vcf400.web.ui` | 101/177 (57.06%) | 12/57 (21.05%) |
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

## 5. PUB400 E2Eシナリオのcurl確認

起動したSpring Bootアプリケーションに対して、シナリオ§1〜§6の代表操作をcurlで確認した。確認ログは `/home/ubuntu/vcf400-pub400-e2e-final.txt` に保存した。

| 節 | 確認内容 | 結果 |
|---|---|---|
| §1 | VCFMAINの4プロフィール・6メニュー、およびSIGN-ON | 成功 |
| §2 | LEARN/400 `Page 0001` → `0002` → `0003`、ページ本文、END | 成功 |
| §3 | 投票成功、同一バッジ重複、NOVOTE拒否、VOTE1のreadonly | 成功 |
| §4 | ゲストブック投稿ID=1、本文参照、展示名・件数表示 | 成功 |
| §5 | EXHBMENUのASHIBATA表示、投票集計 | 成功 |
| §6 | `/admin/db` のVOTINGDB/GUESTBKDB表と登録行 | 成功 |
