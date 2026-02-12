package com.example.SKALA_Mini_Project_1.Users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private Long userId;
    private String email;
    private String name;
    private String message;

    
    
    // 나중에 JWT 토큰 추가 예정
    // private String accessToken;
}