package com.company.projects.airBnbApp.service;

import com.company.projects.airBnbApp.dto.HotelDto;
import com.company.projects.airBnbApp.dto.HotelInfoDto;
import com.company.projects.airBnbApp.dto.HotelInfoRequestDto;

import java.util.List;

public interface HotelService {

    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);
    HotelDto updateHotelById(Long id,HotelDto hotelDto);

    void deleteHotelById(Long id);

    void activateHotel(Long hotelId);

    HotelInfoDto getHotelInfoById(Long hotelId, HotelInfoRequestDto hotelInfoRequestDto);

    List<HotelDto> getAllHotels();
}
