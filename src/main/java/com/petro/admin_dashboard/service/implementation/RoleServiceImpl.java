package com.petro.admin_dashboard.service.implementation;

import com.petro.admin_dashboard.model.Role;
import com.petro.admin_dashboard.repository.RoleRepository;
import com.petro.admin_dashboard.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository<Role> roleRepo;

    @Override
    public Role getRoleByUserId(Long id) {
        return roleRepo.getRoleByUserId(id);
    }
}
