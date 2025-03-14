package com.company.projects.airBnbApp.service;

import com.company.projects.airBnbApp.dto.HotelPriceResponseDto;
import com.company.projects.airBnbApp.dto.HotelSearchRequest;
import com.company.projects.airBnbApp.dto.InventoryDto;
import com.company.projects.airBnbApp.dto.UpdateInventoryRequestDto;
import com.company.projects.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceResponseDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}

