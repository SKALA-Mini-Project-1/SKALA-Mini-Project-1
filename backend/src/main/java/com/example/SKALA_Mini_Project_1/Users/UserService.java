package com.example.SKALA_Mini_Project_1.Users;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SKALA_Mini_Project_1.Users.dto.LoginRequest;
import com.example.SKALA_Mini_Project_1.Users.dto.LoginResponse;
import com.example.SKALA_Mini_Project_1.Users.dto.SignUpRequest;
import com.example.SKALA_Mini_Project_1.Users.dto.SignUpResponse;
import com.example.SKALA_Mini_Project_1.global.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    // 로그인
    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다"));
        
        // 2. 비밀번호 검증 (나중에 암호화 적용 예정)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // 3. JWT 토큰 생성 
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail());
        
        
        // 4. 로그인 성공 응답 생성
        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .message("로그인 성공")
                .accessToken(accessToken)
                .build();
    }

    // 회원가입
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다");
        }
        
        // 2. 사용자 엔티티 생성 (나중에 비밀번호 암호화 추가 예정)
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // ✨ 암호화
                .name(request.getName())
                .phone(request.getPhone())
                .build();
        
        // 3. DB에 저장
        User savedUser = userRepository.save(user);
        
        // 4. 응답 생성
        return SignUpResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .phone(savedUser.getPhone())
                .createdAt(savedUser.getCreatedAt())
                .message("회원가입 성공")
                .build();
    }

    
}