package com.company.projects.airBnbApp.service;

import com.company.projects.airBnbApp.dto.ProfileUpdateRequestDto;
import com.company.projects.airBnbApp.dto.UserDto;
import com.company.projects.airBnbApp.entity.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
