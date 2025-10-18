package com.petro.admin_dashboard.repository.implementation;

import com.petro.admin_dashboard.enumeration.VerificationType;
import com.petro.admin_dashboard.exception.ApiException;
import com.petro.admin_dashboard.mapper.UserRowMapper;
import com.petro.admin_dashboard.model.Role;
import com.petro.admin_dashboard.model.UpdateRequest;
import com.petro.admin_dashboard.model.User;
import com.petro.admin_dashboard.model.UserPrincipal;
import com.petro.admin_dashboard.model.dto.UserDTO;
import com.petro.admin_dashboard.repository.RoleRepository;
import com.petro.admin_dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.petro.admin_dashboard.enumeration.RoleType.ROLE_USER;
import static com.petro.admin_dashboard.enumeration.VerificationType.ACCOUNT;
import static com.petro.admin_dashboard.enumeration.VerificationType.PASSWORD;
import static com.petro.admin_dashboard.query.UserQuery.*;
import static java.util.Map.of;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {
    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository<Role> roleRepo;
    private final BCryptPasswordEncoder encoder;

    @Override
    public User create(User user) {
        if (getEmailCount(user.getEmail().trim().toLowerCase()) > 0)
            throw new ApiException("Email already in use. Please use a different email and try again.");

        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource params = getSqlParameterSource(user);

            jdbc.update(INSERT_USER_QUERY, params, holder);
            user.setId(requireNonNull(holder.getKey()).longValue());

            roleRepo.addRoleToUser(user.getId(), ROLE_USER.name());

            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, of("userId", user.getId(), "url", verificationUrl));

//            emailSvc.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT);
            user.setEnabled(false);
            user.setNotLocked(true);

            return user;
        } catch (Exception ex) {
            throw new ApiException("An error occured, please try again.");
        }
    }

    @Override
    public Collection<User> list(int page, int size) {
        return List.of();
    }

    @Override
    public User get(Long userId) {
        try {
            return jdbc.queryForObject(SELECT_USER_BY_ID, of("id", userId), new UserRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException("No user found by id: " + userId);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException("An error occured, please try again.");
        }
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long userId) {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);

        if (user == null) {
            log.info("User {} not found", email);
            throw new UsernameNotFoundException("User not found");
        } else {
            log.info("User found in database: {}", email);
            return new UserPrincipal(user, roleRepo.getRoleByUserId(user.getId()));
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try {
            return jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, of("email", email), new UserRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException("No user found by email: " + email);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException("An error occured, please try again.");
        }
    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        String expirationDate = format(addDays(new Date(), 1), DATE_FORMAT);
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('A', 'Z')
                .get();
        String verificationCode = generator.generate(8).toUpperCase();

        try {
            jdbc.update(DELETE_VERIFICATION_CODE_BY_USER_ID_QUERY, of("id", user.getId()));
            jdbc.update(INSERT_VERIFICATION_CODE_QUERY,
                    of("user_id", user.getId(), "code", verificationCode, "expiration_date", expirationDate));
            //TODO: Send SMS message
            log.info("Verification code - {}", verificationCode);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException("An error occured, please try again.");
        }
    }

    @Override
    public User verifyCode(String email, String code) {
        if (isVerificationCodeExpired(code)) throw new ApiException("This code has expired. Please login again.");
        try {
            User userByCode = jdbc.queryForObject(SELECT_USER_BY_USER_CODE_QUERY, of("code", code), new UserRowMapper());
            User userByEmail = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, of("email", email), new UserRowMapper());

            if (userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())) {
                jdbc.update(DELETE_CODE, of("code", code));
                return userByCode;
            } else {
                throw new ApiException("Code is invalid, please try again.");
            }
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException("Could not find record.");
        } catch (Exception ex) {
            throw new ApiException("An error occurred, please try again.");
        }
    }

    @Override
    public void resetPassword(String email) {
        if (getEmailCount(email.trim().toLowerCase()) <= 0) {
            throw new ApiException("There is no account for this email address.");
        }

        try {
            String expirationDate = format(addDays(new Date(), 1), DATE_FORMAT);
            User user = getUserByEmail(email);
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), PASSWORD.getType());

            jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, of("userId", user.getId()));
            jdbc.update(INSERT_PASSWORD_VERIFICATION_QUERY, of("userId", user.getId(), "url", verificationUrl, "expirationDate", expirationDate));
            // send email to user
            log.info("Verification url: {}", verificationUrl);
        } catch (Exception ex) {
            throw new ApiException("An error occurred, please try again.");
        }
    }

    @Override
    public User verifyPasswordKey(String key) {
        if (isLinkExpired(key, PASSWORD))
            throw new ApiException("This link has expired. Please reset your password again.");
        try {
            User user = jdbc.queryForObject(SELECT_USER_BY_PASSWORD_URL_QUERY, of("url", getVerificationUrl(key, PASSWORD.getType())), new UserRowMapper());
            jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, of("userId", user.getId()));
            return user;
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException("This link is no longer valid. Please reset your password again.");
        } catch (Exception ex) {
            throw new ApiException("An error occurred, please try again.");
        }
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        if (!password.equals(confirmPassword))
            throw new ApiException("Passwords do not match. Please reset your password again.");
        try {
            jdbc.update(UPDATE_USER_PASSWORD_BY_URL_QUERY, of("password", encoder.encode(password), "url", getVerificationUrl(key, PASSWORD.getType())));
            jdbc.update(DELETE_VERIFICATION_BY_URL_QUERY, of("url", getVerificationUrl(key, PASSWORD.getType())));
        } catch (Exception ex) {
            throw new ApiException("An error occurred, please try again.");
        }
    }

    @Override
    public User verifyAccount(String key) {
        try {
            User user = jdbc.queryForObject(SELECT_USER_BY_ACCOUNT_URL_QUERY, of("url", getVerificationUrl(key, ACCOUNT.getType())), new UserRowMapper());
            jdbc.update(UPDATE_USER_ENABLED_QUERY, of("enabled", true, "id", user.getId()));

            return user;
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException("This link is not valid.");
        } catch (Exception ex) {
            throw new ApiException("An error occurred, please try again.");
        }
    }

    @Override
    public User updateUserDetails(UpdateRequest user) {
        try {
            jdbc.update(UPDATE_USER_DETAILS_QUERY, getUserDetailsSqlParameterSource(user));
            return get(user.getId());
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException("No user found by id: " + user.getId());
        } catch (Exception ex) {
            throw new ApiException("An error occurred, please try again.");
        }
    }

    @Override
    public void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword) {
        if (!newPassword.equals(confirmNewPassword))
            throw new ApiException("Passwords do not match. Please try again.");
        User user = get(id);

        if (encoder.matches(currentPassword, user.getPassword())) {
            try {
                jdbc.update(UPDATE_USER_PASSWORD_BY_ID_QUERY, of("password", encoder.encode(newPassword),  "userId", user.getId()));
            } catch (Exception ex) {
                throw new ApiException("An error occurred, please try again.");
            }
        } else {
            throw new ApiException("Current password is incorrect. Please try again.");
        }
    }

    private Boolean isLinkExpired(String key, VerificationType password) {
        try {
            return jdbc.queryForObject(SELECT_EXPIRATION_BY_URL, of("url", getVerificationUrl(key, password.getType())), Boolean.class);
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException("This link is no longer valid. Please reset your password again.");
        } catch (Exception ex) {
            throw new ApiException("An error occurred, please try again.");
        }
    }

    private Boolean isVerificationCodeExpired(String code) {
        try {
            return jdbc.queryForObject(SELECT_CODE_EXPIRATION_QUERY, of("code", code), Boolean.class);
        } catch (EmptyResultDataAccessException ex) {
            throw new ApiException("This code is no longer valid. Please login again.");
        } catch (Exception ex) {
            throw new ApiException("An error occurred, please try again.");
        }
    }

    private Integer getEmailCount(String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, of("email", email), Integer.class);
    }

    private SqlParameterSource getSqlParameterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", encoder.encode(user.getPassword()));
    }

    private SqlParameterSource getUserDetailsSqlParameterSource(UpdateRequest user) {
        return new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("phone", user.getPhone())
                .addValue("address", user.getAddress())
                .addValue("title", user.getTitle())
                .addValue("bio", user.getBio());
    }

    private String getVerificationUrl(String key, String type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/user/verify/" + type + "/" + key).toUriString();
    }

}
