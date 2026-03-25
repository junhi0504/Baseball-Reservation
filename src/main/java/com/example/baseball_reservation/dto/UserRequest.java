package com.example.baseball_reservation.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String loginId;
    private String password;
    private String name;
}