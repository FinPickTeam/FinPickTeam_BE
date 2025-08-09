package org.scoula.security.util;

import javax.servlet.http.HttpServletResponse;

public class CookieUtil {

    public static void addHttpOnlyCookie(HttpServletResponse resp, String name, String value,
                                         int maxAgeSeconds, boolean secure, String sameSite) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value)
                .append("; Path=/")
                .append("; Max-Age=").append(maxAgeSeconds)
                .append("; HttpOnly");
        if (secure) sb.append("; Secure");
        if (sameSite != null) sb.append("; SameSite=").append(sameSite);
        resp.addHeader("Set-Cookie", sb.toString());
    }

    /** 개발/운영 환경에 맞춘 삭제(secure/sameSite 지정) */
    public static void deleteCookie(HttpServletResponse resp, String name,
                                    boolean secure, String sameSite) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append("")
                .append("; Path=/")
                .append("; Max-Age=0")
                .append("; HttpOnly");
        if (secure) sb.append("; Secure");
        if (sameSite != null) sb.append("; SameSite=").append(sameSite);
        resp.addHeader("Set-Cookie", sb.toString());
    }

    /** 간편 삭제(Lax, secure=false) — 개발 기본값 */
    public static void deleteCookie(HttpServletResponse resp, String name) {
        deleteCookie(resp, name, false, "Lax");
    }
}
