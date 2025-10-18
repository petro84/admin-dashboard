package com.petro.admin_dashboard.service;

import com.petro.admin_dashboard.model.UpdateRequest;
import com.petro.admin_dashboard.model.User;
import com.petro.admin_dashboard.model.dto.UserDTO;
import jakarta.validation.Valid;

public interface UserService {

    UserDTO createUser(User user);

    UserDTO getUserByEmail(String email);

    void sendVerificationCode(UserDTO user);

    UserDTO verifyCode(String email, String code);

    void resetPassword(String email);

    UserDTO verifyPasswordKey(String key);

    void renewPassword(String key, String password, String confirmPassword);

    UserDTO verifyAccount(String key);

    UserDTO updateUserDetails(@Valid UpdateRequest user);

    UserDTO getByUserId(Long userId);
}
