package com.vcf400.service;

import java.util.Map;

/** 元 RPG の定数メッセージ (仕様書 6 章 M-nn)。文言は原文どおり保持する。 */
public final class Messages {
    private Messages() {}

    public static final String ERRBLKBG = "Must enter badge number";          // M-02
    public static final String ERRBLKEX = "Must enter Exhibit ID";            // M-03 / M-10
    public static final String ERRBLKAW = "Must enter Award ID";              // M-04
    public static final String ERREXIST = "You have already voted.";          // M-05
    public static final String ERRPROHB = "Exhibit ineligible for award";     // M-07
    public static final String ERRNOEXB = "Exhibit does not exist";           // M-08
    public static final String ERRNOAWD = "Award does not exist.";            // M-09
    public static final String ERRNONAME = "Must enter your name";            // M-11
    public static final String ERRNOCMT = "Must enter a comment";             // M-12
    public static final String ERRCMTID = "Must enter CommentID";             // M-13
    public static final String ERRHNAME = "Name Hidden";                      // M-14
    public static final String ERRHCMT = "This comment hidden by an admin - offensive content."; // M-14
    public static final String ERRPRIV = "This comment is not part of this guestbook.";          // M-15

    private static final Map<String, String> JA = Map.ofEntries(
            Map.entry(ERRBLKBG, "バッジ番号を入力してください"),
            Map.entry(ERRBLKEX, "展示 ID を入力してください"),
            Map.entry(ERRBLKAW, "アワード ID を入力してください"),
            Map.entry(ERREXIST, "すでに投票済みです。"),
            Map.entry(ERRPROHB, "この展示はアワードの対象外です"),
            Map.entry(ERRNOEXB, "展示が存在しません"),
            Map.entry(ERRNOAWD, "アワードが存在しません。"),
            Map.entry(ERRNONAME, "お名前を入力してください"),
            Map.entry(ERRNOCMT, "コメントを入力してください"),
            Map.entry(ERRCMTID, "コメント ID を入力してください"),
            Map.entry(ERRHNAME, "名前は非表示"),
            Map.entry(ERRHCMT, "このコメントは管理者により非表示にされています (不適切な内容)。"),
            Map.entry(ERRPRIV, "このコメントはこのゲストブックのものではありません。"));

    /** 英語原文 (M-xx) に対応する日本語文言。未知の文言はそのまま返す。 */
    public static String ja(String en) {
        return en == null ? "" : JA.getOrDefault(en, en);
    }

    public static final String MSG_MENU_NOT_IN_LIB = "メニュー番号 %s は VCF/400 ライブラリに含まれていません。";
    public static final String MSG_MENU_INVALID = "無効なオプションです。メニュー番号を入力して ENTER を押してください。";
    public static final String MSG_KIOSK_SELECT = "メニュー番号を選択して ENTER を押してください。";
    public static final String MSG_KIOSK_NOT_FOUND = "展示 %s が見つかりません。";
    public static final String MSG_KIOSK_ENDED = "キオスク %s を終了しました。";
}
