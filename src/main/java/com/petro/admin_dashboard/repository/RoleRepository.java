package com.petro.admin_dashboard.repository;

import com.petro.admin_dashboard.model.Role;

import java.util.Collection;

public interface RoleRepository<T extends Role> {
    T create(T data);
    Collection<T> list();
    T get(Long userId);
    T update(T data);
    Boolean delete(Long userId);

    void addRoleToUser(Long userId, String roleName);
    Role getRoleByUserId(Long userId);
    Role getRoleByUserEmail(String email);
    void updateUserRole(Long userId, String roleName);
}
