package com.petro.admin_dashboard.service;

import com.petro.admin_dashboard.model.Role;

import java.util.Collection;

public interface RoleService {
    Role getRoleByUserId(Long id);
    Collection<Role> getRoles();
}
