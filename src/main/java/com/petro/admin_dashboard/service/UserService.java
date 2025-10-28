package com.petro.admin_dashboard.service;

import com.petro.admin_dashboard.model.UpdateRequest;
import com.petro.admin_dashboard.model.User;
import com.petro.admin_dashboard.model.dto.UserDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

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

    void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword);

    void updateUserRole(Long id, String roleName);

    void updateAccountSettings(Long id, Boolean enabled, Boolean notLocked);

    UserDTO toggleMfa(String email);

    void updateImage(UserDTO user, MultipartFile image);
}
