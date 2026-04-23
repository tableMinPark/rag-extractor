package com.genai.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genai.auth.service.MemberService;
import com.genai.auth.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@RequiredArgsConstructor
public class TokenReissueFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MemberService memberService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().equals("/api/auth/reissue") ||
               !"POST".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null || !jwtUtil.isValid(refreshToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String username = jwtUtil.getUsername(refreshToken);
        UserDetails userDetails = memberService.loadUserByUsername(username);
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        String newAccessToken = jwtUtil.generateAccessToken(username, role);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of("accessToken", newAccessToken));
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> jwtUtil.getRefreshTokenCookieName().equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
