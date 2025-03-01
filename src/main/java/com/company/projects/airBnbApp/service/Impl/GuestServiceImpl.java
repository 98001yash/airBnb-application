package com.company.projects.airBnbApp.service.Impl;

import com.company.projects.airBnbApp.dto.GuestDto;
import com.company.projects.airBnbApp.entity.Guest;
import com.company.projects.airBnbApp.entity.User;
import com.company.projects.airBnbApp.exception.ResourceNotFoundException;
import com.company.projects.airBnbApp.exception.UnAuthorisedException;
import com.company.projects.airBnbApp.repository.GuestRepository;
import com.company.projects.airBnbApp.service.GuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.company.projects.airBnbApp.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<GuestDto> getAllGuests() {
        User user  = getCurrentUser();
        log.info("Fetching all guests of user with id: {}",user.getId());
        List<Guest> guests = guestRepository.findByUser(user);
        return guests.stream()
                .map(guest ->modelMapper.map(guest,GuestDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void updateGuest(Long guestId, GuestDto guestDto) {
     log.info("Updating guest with id: {}",guestId);
     Guest guest = guestRepository.findById(guestId)
             .orElseThrow(()->new ResourceNotFoundException("Guest not found"));
     User user = getCurrentUser();
     if(!user.equals(guest.getUser())) throw new UnAuthorisedException("You are not the owner of this guest");

     modelMapper.map(guestDto,guest);
     guest.setUser(user);
     guest.setId(guestId);

     guestRepository.save(guest);
     log.info("Guest with ID:{} updated succesfully",guestId);
    }

    @Override
    public void deleteGuest(Long guestId) {
   log.info("Deleting guest with id: {}",guestId);
   Guest guest = guestRepository.findById(guestId)
           .orElseThrow(()->new ResourceNotFoundException("Guest not found"));

   User user = getCurrentUser();
   if(!user.equals(guest.getUser())) throw new UnAuthorisedException("You are not the owner of this guest");

   guestRepository.deleteById(guestId);
   log.info("Guest with ID: {} deleted successfully",guestId);
    }

    @Override
    public GuestDto addNewGuest(GuestDto guestDto) {
        log.info("Adding new quest: {}",guestDto);
        User user = getCurrentUser();
        Guest guest = modelMapper.map(guestDto,Guest.class);
        guest.setUser(user);
        Guest savedGuest = guestRepository.save(guest);
        log.info("Guest added with ID: {}",savedGuest.getId());
        return modelMapper.map(savedGuest,GuestDto.class);
    }
}
