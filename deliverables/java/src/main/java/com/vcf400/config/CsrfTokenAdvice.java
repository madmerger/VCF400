package com.vcf400.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 画面 (Thymeleaf) 用に `csrfToken` をモデルへ供給する。初回アクセスでセッションに
 * synchronizer token を遅延生成し、全フォームの hidden `_csrf` が参照する。
 */
@ControllerAdvice
public class CsrfTokenAdvice {

    @ModelAttribute("csrfToken")
    public String csrfToken(HttpSession session) {
        return CsrfFilter.token(session);
    }
}
