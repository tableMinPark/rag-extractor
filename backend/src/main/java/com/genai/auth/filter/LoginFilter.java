package com.genai.auth.filter;

import com.genai.auth.dto.request.LoginRequestDto;
import com.genai.auth.dto.response.LoginResponseDto;
import com.genai.global.enums.Response;
import com.genai.auth.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

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
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        setFilterProcessesUrl("/api/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequestDto userRequestDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
            log.info("userRequestDto : {}", userRequestDto);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userRequestDto.getUsername(), userRequestDto.getPassword());
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        String username = authResult.getName();

        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.generateAccessToken(username, role);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        Cookie cookie = new Cookie(jwtUtil.getRefreshTokenCookieName(), refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtUtil.getRefreshTokenExpiry() / 1000));
        response.addCookie(cookie);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Response.LOGIN_SUCCESS.toResponseDto(new LoginResponseDto(username, role, accessToken))
        ));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Response.LOGIN_FAIL.toResponseDto()));
    }
}
