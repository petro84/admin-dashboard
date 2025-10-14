package com.petro.admin_dashboard.service;

import com.petro.admin_dashboard.model.Role;

public interface RoleService {
    Role getRoleByUserId(Long id);
}
