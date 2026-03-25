package com.example.baseball_reservation.service;

import com.example.baseball_reservation.dto.UserRequest;
import com.example.baseball_reservation.entity.Role;
import com.example.baseball_reservation.entity.User;
import com.example.baseball_reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(UserRequest request) {
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        User user = User.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword())) // 암호화 저장
                .name(request.getName())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    // 로그인 검증 로직 (나중에 JWT 발급 시 사용)
    public User login(String loginId, String password) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 틀렸습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 틀렸습니다.");
        }
        return user;
    }
}