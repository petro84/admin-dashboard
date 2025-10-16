package com.petro.admin_dashboard.service.implementation;

import com.petro.admin_dashboard.model.Role;
import com.petro.admin_dashboard.model.User;
import com.petro.admin_dashboard.model.dto.UserDTO;
import com.petro.admin_dashboard.repository.RoleRepository;
import com.petro.admin_dashboard.repository.UserRepository;
import com.petro.admin_dashboard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.petro.admin_dashboard.mapper.UserDTOMapper.fromUser;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository<User> userRepo;
    private final RoleRepository<Role> roleRepo;

    @Override
    public UserDTO createUser(User user) {
        return mapToUserDTO(userRepo.create(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return mapToUserDTO(userRepo.getUserByEmail(email));
    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        userRepo.sendVerificationCode(user);
    }

    @Override
    public UserDTO verifyCode(String email, String code) {
        return mapToUserDTO(userRepo.verifyCode(email, code));
    }

    @Override
    public void resetPassword(String email) {
        userRepo.resetPassword(email);
    }

    @Override
    public UserDTO verifyPasswordKey(String key) {
        return mapToUserDTO(userRepo.verifyPasswordKey(key));
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        userRepo.renewPassword(key, password, confirmPassword);
    }

    @Override
    public UserDTO verifyAccount(String key) {
        return mapToUserDTO(userRepo.verifyAccount(key));
    }

    private UserDTO mapToUserDTO(User user) {
        return fromUser(user, roleRepo.getRoleByUserId(user.getId()));
    }
}
