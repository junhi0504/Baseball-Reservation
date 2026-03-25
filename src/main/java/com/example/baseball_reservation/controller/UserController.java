package com.example.baseball_reservation.controller;

import com.example.baseball_reservation.config.JwtTokenProvider; // config 패키지로 수정
import com.example.baseball_reservation.dto.LoginResponse;
import com.example.baseball_reservation.dto.UserRequest;
import com.example.baseball_reservation.entity.User;
import com.example.baseball_reservation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    // 1. 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserRequest request) {
        userService.signUp(request);
        return ResponseEntity.ok("회원가입 완료!");
    }

    // 2. 로그인 API (성공 시 JWT 토큰 발득)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserRequest request) {
        // 1. 아이디/비번 검증 (UserService의 login 메서드 사용)
        User user = userService.login(request.getLoginId(), request.getPassword());

        // 2. 토큰 생성 (JwtTokenProvider 사용)
        String token = tokenProvider.createToken(user.getLoginId());

        // 3. 토큰과 유저 이름을 응답
        return ResponseEntity.ok(new LoginResponse(token, user.getName()));
    }
}