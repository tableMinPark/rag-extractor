package com.genai.auth.service;

import com.genai.common.exception.NotFoundException;
import com.genai.auth.repository.MemberRepository;
import com.genai.auth.repository.entity.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        try {
            MemberEntity memberEntity = memberRepository.findByEmail(username)
                    .orElseThrow(() ->  new NotFoundException("회원 정보"));

            return User.builder()
                    .username(memberEntity.getName())
                    .password(memberEntity.getPassword())
                    .roles(memberEntity.getRole())
                    .build();

        } catch (NotFoundException e) {
            throw new UsernameNotFoundException("User not found");
        }
    }

    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("manager1!"));
        System.out.println(new BCryptPasswordEncoder().encode("trainer1!"));
    }
}