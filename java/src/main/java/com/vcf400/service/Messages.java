package com.vcf400.service;

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
}
