package com.company.projects.airBnbApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelInfoRequestDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long roomsCount;
}

