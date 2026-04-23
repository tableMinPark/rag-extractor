package com.genai.auth.service;

import com.genai.auth.dto.request.RegisterRequestDto;

public interface AuthService {

    void register(RegisterRequestDto requestDto);
}
