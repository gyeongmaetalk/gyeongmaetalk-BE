package auctionTalk.auction.utils;

import auctionTalk.auction.config.security.jwt.JwtToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.util.SerializationUtils;

import java.io.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class CookieUtils {

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 60L * 60 * 1000;           // 1시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 15L * 24 * 60 * 60 * 1000; // 15일

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst();
    }

    public static void addJwtCookies(HttpServletResponse response, JwtToken token) {

        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", token.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMillis(ACCESS_TOKEN_EXPIRE_TIME))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", token.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    public static void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie access = ResponseCookie.from("ACCESS_TOKEN", "")
                .httpOnly(true).secure(true).sameSite("None")
                .path("/").maxAge(0).build();

        ResponseCookie refresh = ResponseCookie.from("REFRESH_TOKEN", "")
                .httpOnly(true).secure(true).sameSite("None")
                .path("/").maxAge(0).build();

        response.addHeader(HttpHeaders.SET_COOKIE, access.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refresh.toString());
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)   // 즉시 삭제
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public static String serialize(Object obj) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(obj));
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(
                SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue()))
        );
    }
}