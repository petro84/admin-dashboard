package com.petro.admin_dashboard.repository.implementation;

import com.petro.admin_dashboard.exception.ApiException;
import com.petro.admin_dashboard.mapper.RoleRowMapper;
import com.petro.admin_dashboard.model.Role;
import com.petro.admin_dashboard.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.petro.admin_dashboard.enumeration.RoleType.ROLE_USER;
import static com.petro.admin_dashboard.query.RoleQuery.*;
import static java.util.Map.of;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository<Role> {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Role create(Role data) {
        return null;
    }

    @Override
    public Collection<Role> list(int page, int size) {
        return List.of();
    }

    @Override
    public Role get(Long userId) {
        return null;
    }

    @Override
    public Role update(Role data) {
        return null;
    }

    @Override
    public Boolean delete(Long userId) {
        return null;
    }

    @Override
    public void addRoleToUser(Long userId, String roleName) {
        try {
            Role role = jdbc.queryForObject(SELECT_ROLE_BY_NAME_QUERY, of("name", roleName), new RoleRowMapper());
            jdbc.update(INSERT_ROLE_TO_USER_QUERY, of("userId", userId, "roleId", Objects.requireNonNull(role).getId()));
        } catch (EmptyResultDataAccessException ex) {
          throw new ApiException("No role found by name: " + ROLE_USER.name());
        } catch (Exception ex) {
            throw new ApiException("An error occurred, please try again.");
        }
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        try {
            return jdbc.queryForObject(SELECT_ROLE_BY_ID_QUERY, of("id", userId), new RoleRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException("No role found by name " + ROLE_USER.name());
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException("An error occurred, please try again.");
        }
    }

    @Override
    public Role getRoleByUserEmail(String email) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, Role roleName) {

    }
}
