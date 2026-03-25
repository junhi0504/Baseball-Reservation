package com.example.baseball_reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AvailableSeatResponse {
    private Long seatId;
    private String zone;
    private int seatNumber;
}