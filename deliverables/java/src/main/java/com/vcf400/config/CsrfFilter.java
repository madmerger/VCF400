package com.vcf400.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * CSRF 対策 (synchronizer token)。セッション属性 {@link #SESSION_KEY} に乱数トークンを
 * 持ち (遅延生成は CsrfTokenAdvice)、POST/PUT/DELETE はフォームの `_csrf` パラメータ
 * または `X-CSRF-Token` ヘッダがトークンと一致する要求だけ通す。
 * `/api/db/` (クロス検証 API: フラグ制御 + ループバック限定、セッションを持たない
 * node fetch から使う) と `/h2` (開発コンソール、既定 OFF) は対象外。
 */
@Component
public class CsrfFilter extends OncePerRequestFilter {
    public static final String SESSION_KEY = "vcf.csrfToken";
    public static final String HEADER = "X-CSRF-Token";
    public static final String PARAM = "_csrf";

    private static final SecureRandom RANDOM = new SecureRandom();

    /** セッションのトークンを返す。なければ生成する (呼び出し側で新規セッションを許可すること)。 */
    public static String token(HttpSession session) {
        Object v = session.getAttribute(SESSION_KEY);
        if (v instanceof String s && !s.isEmpty()) {
            return s;
        }
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_KEY, token);
        return token;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!isStateChanging(request.getMethod()) || path.startsWith("/api/db/") || path.startsWith("/h2")) {
            chain.doFilter(request, response);
            return;
        }
        String presented = request.getParameter(PARAM);
        if (presented == null || presented.isEmpty()) {
            presented = request.getHeader(HEADER);
        }
        HttpSession session = request.getSession(false);
        Object expected = session == null ? null : session.getAttribute(SESSION_KEY);
        if (presented == null || presented.isEmpty() || !(expected instanceof String e) || e.isEmpty()
                || !MessageDigest.isEqual(
                        presented.getBytes(StandardCharsets.UTF_8), e.getBytes(StandardCharsets.UTF_8))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean isStateChanging(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
    }
}
