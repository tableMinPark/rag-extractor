package com.genai.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genai.auth.domain.Member;
import com.genai.auth.dto.request.LoginRequestDto;
import com.genai.auth.dto.response.LoginResponseDto;
import com.genai.global.security.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public LoginFilter(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            ObjectMapper objectMapper
    ) {
        super(authenticationManager);
        setFilterProcessesUrl("/api/auth/login");
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequestDto userRequestDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
            log.info("{}", userRequestDto);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userRequestDto.getUserId(), userRequestDto.getPassword());
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        Member member = (Member) authResult.getPrincipal();
        String accessToken = jwtUtil.generateAccessToken(member.getUserId(), member.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(member.getUserId());

        Cookie cookie = new Cookie(jwtUtil.getRefreshTokenCookieName(), refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtUtil.getRefreshTokenExpiry() / 1000));
        response.addCookie(cookie);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                new LoginResponseDto(accessToken, member.getUserId(), member.getName(), member.getRole())
        ));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다.")
        ));
    }
}
