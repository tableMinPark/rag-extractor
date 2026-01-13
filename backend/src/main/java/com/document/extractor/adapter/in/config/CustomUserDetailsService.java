package com.document.extractor.adapter.in.config;

import com.document.extractor.application.command.GetMemberCommand;
import com.document.extractor.application.exception.NotFoundException;
import com.document.extractor.application.usecase.MemberUseCase;
import com.document.extractor.domain.model.Member;
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

    private final MemberUseCase memberUseCase;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        try {
            Member member = memberUseCase.getMemberUseCase(GetMemberCommand.builder()
                    .name(username)
                    .build());

            return User.builder()
                    .username(member.getName())
                    .password(member.getPassword())
                    .roles(member.getRole())
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