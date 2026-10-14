package com.vcf400.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 簡易 CSRF 対策 (SameSite=strict Cookie に加える多層防御)。
 * POST/PUT/DELETE は Sec-Fetch-Site: cross-site または Origin がリクエスト元と
 * 不一致 (scheme://host:port) のとき 403。Origin も Sec-Fetch-Site もない
 * ブラウザ外クライアント (curl, node fetch, MockMvc) は許可する。
 * X-Forwarded-* は forward-headers 戦略を設定していないため参照しない。
 */
@Component
public class CsrfFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!isStateChanging(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        String fetchSite = request.getHeader("Sec-Fetch-Site");
        if ("cross-site".equalsIgnoreCase(fetchSite)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String origin = request.getHeader("Origin");
        if (origin != null && !sameOrigin(origin, request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean isStateChanging(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
    }

    private static boolean sameOrigin(String origin, HttpServletRequest request) {
        try {
            URI o = new URI(origin);
            if (o.getHost() == null || !o.getHost().equalsIgnoreCase(request.getServerName())) {
                return false;
            }
            String scheme = o.getScheme() == null ? "" : o.getScheme();
            if (!scheme.equalsIgnoreCase(request.getScheme())) {
                return false;
            }
            return effectivePort(o.getPort(), scheme) == effectivePort(
                    request.getServerPort(), request.getScheme());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static int effectivePort(int port, String scheme) {
        if (port >= 0) {
            return port;
        }
        return "https".equalsIgnoreCase(scheme) ? 443 : 80;
    }
}
