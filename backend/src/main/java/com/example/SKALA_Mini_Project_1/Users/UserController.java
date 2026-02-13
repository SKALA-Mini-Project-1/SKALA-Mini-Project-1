package com.example.SKALA_Mini_Project_1.Users;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SKALA_Mini_Project_1.Common.ErrorResponse;
import com.example.SKALA_Mini_Project_1.Users.dto.LoginRequest;
import com.example.SKALA_Mini_Project_1.Users.dto.LoginResponse;
import com.example.SKALA_Mini_Project_1.Users.dto.SignUpRequest;
import com.example.SKALA_Mini_Project_1.Users.dto.SignUpResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final UserRepository userRepository;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponse error = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        try {
            SignUpResponse response = userService.signUp(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            ErrorResponse error = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // 로그아웃 API (Swagger 호환)
    @Operation(summary = "로그아웃")
@PostMapping("/logout")
public ResponseEntity<?> logout(
        @Parameter(hidden = true) 
        @RequestHeader("Authorization") String authHeader  // ✨ 단순하게!
) {
    try {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), 
                "Authorization 헤더 형식이 올바르지 않습니다"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        String token = authHeader.substring(7);
        userService.logout(token);
        
        return ResponseEntity.ok(Map.of(
            "message", "로그아웃 성공",
            "status", "success"
        ));
        
    } catch (IllegalArgumentException e) {
        ErrorResponse error = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    } catch (Exception e) {
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
            "로그아웃 처리 중 오류가 발생했습니다"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

    // JWT 인증 테스트용 엔드포인트
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() {
        // 현재 로그인한 사용자 ID 가져오기
        Long userId = (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        
        return ResponseEntity.ok(Map.of(
            "userId", user.getId(),
            "email", user.getEmail(),
            "name", user.getName(),
            "message", "인증된 사용자 정보 조회 성공"
        ));
    }

    
    // Validation 예외 처리 추가
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

}