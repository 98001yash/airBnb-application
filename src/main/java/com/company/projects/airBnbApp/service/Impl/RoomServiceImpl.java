package com.company.projects.airBnbApp.service.Impl;

import com.company.projects.airBnbApp.dto.RoomDto;
import com.company.projects.airBnbApp.entity.Hotel;
import com.company.projects.airBnbApp.entity.Room;
import com.company.projects.airBnbApp.entity.User;
import com.company.projects.airBnbApp.exception.ResourceNotFoundException;
import com.company.projects.airBnbApp.exception.UnAuthorisedException;
import com.company.projects.airBnbApp.repository.HotelRepository;
import com.company.projects.airBnbApp.repository.RoomRepository;
import com.company.projects.airBnbApp.service.InventoryService;
import com.company.projects.airBnbApp.service.RoomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.company.projects.airBnbApp.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService  inventoryService;
    private final ModelMapper modelMapper;


    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
       log.info("Creating a new room in hotel with Id: [}",hotelId);
       Hotel hotel = hotelRepository.findById(hotelId)
               .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id: "+hotelId));

       User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       if(!user.equals(hotel.getOwner())){
           throw new UnAuthorisedException("This user does not  own this hotel with id : "+hotelId);
       }
       Room room = modelMapper.map(roomDto, Room.class);
       room.setHotel(hotel);
       room = roomRepository.save(room);

        if (hotel.getActive()) {

            inventoryService.initializeRoomForAYear(room);
        }
        return modelMapper.map(room,RoomDto.class);

    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms in hotel with ID: {}",hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id: "+hotelId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        return hotel.getRooms()
                .stream()
                .map((element)-> modelMapper.map(element,RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room with ID: {}",roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with ID: "+roomId));
        return modelMapper.map(room,RoomDto.class);
    }



    @Transactional
    @Override
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the room with ID:{}",roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with ID: "+roomId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorisedException("This user does not own this room with id: "+roomId);
        }

        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);

    }

    @Override
    @Transactional
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {
      log.info("Updating the room with ID: {}",roomId);
      Hotel hotel= hotelRepository
              .findById(hotelId)
              .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID: "+hotelId));

      User user  = getCurrentUser();
      if(!user.equals(hotel.getOwner())){
          throw new UnAuthorisedException("this user does not own this hotel with ID: "+hotelId);
      }

      Room room = roomRepository.findById(roomId)
              .orElseThrow(()->new ResourceNotFoundException("Room not found with id: "+roomId));

      modelMapper.map(roomDto,room);
      room.setId(roomId);

 //  TODO: if price or inventory is updated, then update the inventory for this room

        room = roomRepository.save(room);
        return modelMapper.map(room,RoomDto.class);
    }
}
