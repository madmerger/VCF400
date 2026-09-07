package com.vcf400.web;

import com.vcf400.domain.Launch;
import jakarta.servlet.http.HttpSession;

/**
 * 5250 ジョブの状態 (サインオンユーザー = LAUNCH、呼び出し元) を HTTP セッションで再現する。
 * returnTo は「プログラム終了 → 呼び出し元 (VCFMAIN またはキオスクメニュー) に戻る」先。
 */
public final class Nav {
    private Nav() {}

    public static final String LAUNCH = "vcf.launch";
    public static final String RETURN_TO = "vcf.returnTo";
    public static final String MENU = "/menu";

    public static Launch launch(HttpSession session, String defaultProfile) {
        Object v = session.getAttribute(LAUNCH);
        if (v instanceof Launch l) {
            return l;
        }
        Launch l = new Launch(defaultProfile);
        session.setAttribute(LAUNCH, l);
        return l;
    }

    public static void setLaunch(HttpSession session, String profile) {
        session.setAttribute(LAUNCH, new Launch(profile));
    }

    public static String returnTo(HttpSession session) {
        Object v = session.getAttribute(RETURN_TO);
        return v instanceof String s && !s.isEmpty() ? s : MENU;
    }

    public static void setReturnTo(HttpSession session, String url) {
        session.setAttribute(RETURN_TO, url);
    }
}
