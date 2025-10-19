package com.petro.admin_dashboard.repository;

import com.petro.admin_dashboard.model.UpdateRequest;
import com.petro.admin_dashboard.model.User;
import com.petro.admin_dashboard.model.dto.UserDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

public interface UserRepository<T extends User> {
    T create(T data);
    Collection<T> list(int page, int size);
    T get(Long userId);
    T update(T data);
    Boolean delete(Long userId);

    T getUserByEmail(String email);

    void sendVerificationCode(UserDTO user);

    T verifyCode(String email, String code);

    void resetPassword(String email);

    T verifyPasswordKey(String key);

    void renewPassword(String key, String password, String confirmPassword);

    T verifyAccount(String key);

    T updateUserDetails(UpdateRequest user);

    void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword);

    void updateAccountSettings(Long userId, Boolean enabled, Boolean notLocked);

    T toggleMfa(String email);

    void updateImage(UserDTO user, MultipartFile image);
}
