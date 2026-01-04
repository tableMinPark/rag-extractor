package com.document.extractor.adapter.in.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 기본 관리자 사용자
        if ("admin".equals(username)) {
            return User.builder()
                    .username("admin")
                    .password("$2a$10$cH7t5bKohqOLsinLWOenaulHR/7jdlismKhGDSFPZdJUgw3jzuzVO")
                    .roles("ADMIN")
                    .build();
        } else if ("manager".equals(username)) {
            return User.builder()
                    .username("manager")
                    .password("$2a$10$0/nMpN32/Nr1TWtLPD1gq.yCYVd29p55SH0O/Dxedfzzb5K6vXX4G")
                    .roles("MANAGER")
                    .build();
        } else if ("trainer".equals(username)) {
            return User.builder()
                    .username("trainer")
                    .password("$2a$10$qM0Wgg7MOZHzheIH13nwYuvCiaXxKjfkVANp2Qkcet/5ndFVCzAry")
                    .roles("NORMAL")
                    .build();
        }

        throw new UsernameNotFoundException("User not found");
    }

    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("manager1!"));
        System.out.println(new BCryptPasswordEncoder().encode("trainer1!"));
    }
}