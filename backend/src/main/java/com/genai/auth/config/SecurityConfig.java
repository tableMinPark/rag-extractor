package com.genai.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genai.auth.filter.JwtFilter;
import com.genai.auth.filter.LoginFilter;
import com.genai.auth.filter.TokenReissueFilter;
import com.genai.auth.service.MemberService;
import com.genai.auth.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final UserDetailsService userDetailsService;
    private final MemberService memberService;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/api/login", "/api/auth/reissue", "/").permitAll()
                .requestMatchers("/api/batch/**").hasAnyRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/chunk/**").hasAnyRole("ADMIN", "MANAGER")
                .anyRequest().authenticated());

        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, objectMapper);
        loginFilter.setFilterProcessesUrl("/api/login");

        TokenReissueFilter tokenReissueFilter = new TokenReissueFilter(jwtUtil, memberService, objectMapper);

        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        JwtFilter jwtFilter = new JwtFilter(jwtUtil);
        http.addFilterBefore(jwtFilter, LoginFilter.class);
        http.addFilterBefore(tokenReissueFilter, JwtFilter.class);

        return http.build();
    }
}
