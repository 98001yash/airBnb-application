package com.company.projects.airBnbApp.service.Impl;

import com.company.projects.airBnbApp.dto.*;
import com.company.projects.airBnbApp.entity.Hotel;
import com.company.projects.airBnbApp.entity.Room;
import com.company.projects.airBnbApp.entity.User;
import com.company.projects.airBnbApp.exception.ResourceNotFoundException;
import com.company.projects.airBnbApp.exception.UnAuthorisedException;
import com.company.projects.airBnbApp.repository.HotelRepository;
import com.company.projects.airBnbApp.repository.InventoryRepository;
import com.company.projects.airBnbApp.repository.RoomRepository;
import com.company.projects.airBnbApp.service.HotelService;
import com.company.projects.airBnbApp.service.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.company.projects.airBnbApp.AppUtils.getCurrentUser;


@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
       log.info("Creating a new hotel with name: {}",hotelDto.getName());
       Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
       hotel.setActive(false);

       User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       hotel.setOwner(user);

       hotel = hotelRepository.save(hotel);
       log.info("Created a new hotel with id: {}",hotelDto.getId());
       return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting the hotel with ID: {}",id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+id);
        }
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
       log.info("Updating the hotel with ID: {}",id);
       Hotel hotel = hotelRepository
               .findById(id)
               .orElseThrow(()->
                       new ResourceNotFoundException("Hotel not found with ID: "+id));

       User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       if(!user.equals(hotel.getOwner())){
           throw new UnAuthorisedException("this user does not own this hotel with id: "+id);
       }

       modelMapper.map(hotelDto,hotel);
       hotel.setId(id);
       hotel = hotelRepository.save(hotel);
       return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id: "+id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this model with id: "+id);
        }

        for(Room room: hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating the hotel with ID: {}",hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own  this hotel with id: "+hotelId);
        }
        hotel.setActive(true);

        // assembling only do it once
        for(Room room: hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId, HotelInfoRequestDto hotelInfoRequestDto) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id: "+hotelId));

        long daysCount = ChronoUnit.DAYS.between(hotelInfoRequestDto.getStartDate(),hotelInfoRequestDto.getEndDate())+1;

        List<RoomPriceDto> roomPriceDtoList = inventoryRepository.findRoomAveragePrice(hotelId,hotelInfoRequestDto.getStartDate(),hotelInfoRequestDto.getEndDate(),
                hotelInfoRequestDto.getRoomsCount(),daysCount);

        List<RoomPriceResponseDto> rooms = roomPriceDtoList.stream()
                .map(roomPriceDto ->{
                    RoomPriceResponseDto roomPriceResponseDto = modelMapper.map(roomPriceDto.getRoom(),
                            RoomPriceResponseDto.class);
                    roomPriceResponseDto.setPrice(roomPriceDto.getPrice());
                    return roomPriceResponseDto;
                })
                .collect(Collectors.toList());
        return  new HotelInfoDto(modelMapper.map(hotel,HotelDto.class),rooms);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        User user = getCurrentUser();
        log.info("Getting all hotels for the admin user with ID: {}",user.getId());
        List<Hotel> hotels = hotelRepository.findByOwner(user);

        return hotels
                .stream()
                .map((element)->modelMapper.map(element,HotelDto.class))
                .collect(Collectors.toList());
    }
}
