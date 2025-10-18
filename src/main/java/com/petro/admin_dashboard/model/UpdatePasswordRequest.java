package com.petro.admin_dashboard.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordRequest {
    @NotEmpty(message = "Current password cannot be empty")
    private String currentPassword;
    @NotEmpty(message = "New password cannot be empty")
    private String newPassword;
    @NotEmpty(message = "Confirm password cannot be empty")
    private String confirmNewPassword;
}
