package com.genai.auth.service;

import com.genai.auth.controller.dto.request.RegisterRequestDto;
import com.genai.auth.repository.MemberRepository;
import com.genai.auth.repository.entity.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(RegisterRequestDto requestDto) {
        if (memberRepository.existsByUserId(requestDto.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        MemberEntity member = MemberEntity.builder()
                .userId(requestDto.getUserId())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .name(requestDto.getName())
                .email(StringUtils.hasText(requestDto.getEmail()) ? requestDto.getEmail() : null)
                .role("ROLE_USER")
                .build();

        memberRepository.save(member);
    }
}
