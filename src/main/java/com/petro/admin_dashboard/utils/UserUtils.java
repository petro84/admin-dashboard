package com.petro.admin_dashboard.utils;

import com.petro.admin_dashboard.model.UserPrincipal;
import com.petro.admin_dashboard.model.dto.UserDTO;
import org.springframework.security.core.Authentication;

public class UserUtils {
    public static UserDTO getAuthenticatedUser(Authentication authentication) {
        return (UserDTO) authentication.getPrincipal();
    }

    public static UserDTO getLoggedInUser(Authentication auth) {
        return ((UserPrincipal) auth.getPrincipal()).getUser();
    }

}
