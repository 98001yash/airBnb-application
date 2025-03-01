package com.company.projects.airBnbApp.repository;

import com.company.projects.airBnbApp.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room,Long> {
}
