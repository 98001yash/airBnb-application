package com.company.projects.airBnbApp.repository;

import com.company.projects.airBnbApp.entity.Booking;
import com.company.projects.airBnbApp.entity.Hotel;
import com.company.projects.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {

    Optional<Booking> findByPaymentSessionId(String sessionId);

     List<Booking> findByHotel(Hotel hotel);

     List<Booking> findHotelAndCreatedBetween(Hotel hotel, LocalDateTime startDate, LocalDateTime endDateBetween);

     List<Booking> findByUser(User user);
}
