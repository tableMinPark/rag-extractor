package com.genai.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisterRequestDto {

    @NotBlank
    @Size(min = 3, max = 50)
    private String userId;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Pattern(
            regexp = "^$|^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$",
            message = "올바른 이메일 형식이어야 합니다"
    )
    @Size(max = 200)
    private String email;
}
