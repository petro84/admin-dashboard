package com.petro.admin_dashboard.controller;

import com.petro.admin_dashboard.event.NewUserEvent;
import com.petro.admin_dashboard.exception.ApiException;
import com.petro.admin_dashboard.model.*;
import com.petro.admin_dashboard.model.dto.UserDTO;
import com.petro.admin_dashboard.provider.TokenProvider;
import com.petro.admin_dashboard.service.EventService;
import com.petro.admin_dashboard.service.RoleService;
import com.petro.admin_dashboard.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.petro.admin_dashboard.enumeration.EventType.*;
import static com.petro.admin_dashboard.mapper.UserDTOMapper.toUser;
import static com.petro.admin_dashboard.utils.ExceptionUtils.processError;
import static com.petro.admin_dashboard.utils.UserUtils.*;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/user")
public class UserController {
    private final UserService userSvc;
    private final AuthenticationManager authManager;
    private final TokenProvider tokenProvider;
    private final RoleService roleSvc;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ApplicationEventPublisher publisher;
    private final EventService eventSvc;

    private static final String TOKEN_PREFIX = "Bearer ";

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user) {
        UserDTO userDTO = userSvc.createUser(user);
        return ResponseEntity.created(getURI())
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userDTO))
                        .message("User Created")
                        .status(CREATED)
                        .statusCode(CREATED.value())
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        UserDTO user = authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        return user.isUsingMfa() ? sendVerificationCode(user) : sendResponse(user);
    }

    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code) {
        UserDTO user = userSvc.verifyCode(email, code);
        publisher.publishEvent(new NewUserEvent(user.getEmail(), LOGIN_ATTEMPT_SUCCESS));
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user, "access_token", tokenProvider.createAccessToken(getUserPrincipal(user)),
                                "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(user))))
                        .message("User logged in successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> getProfile(Authentication authentication) {
        UserDTO user = userSvc.getUserByEmail(getAuthenticatedUser(authentication).getEmail());
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user, "roles", roleSvc.getRoles(), "events", eventSvc.getEventsByUserId(user.getId())))
                        .message("Profile Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/reset-password/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) {
        userSvc.resetPassword(email);
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Email sent. Check inbox to reset password.")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyPasswordUrl(@PathVariable("key") String key) {
        UserDTO user = userSvc.verifyPasswordKey(key);
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user))
                        .message("Please enter a new password")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PostMapping("/reset-password/{key}/{password}/{confirmPassword}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("key") String key,
                                                      @PathVariable("password") String password,
                                                      @PathVariable("confirmPassword") String confirmPassword) {
        userSvc.renewPassword(key, password, confirmPassword);
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Password has been reset.")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/verify/account/{key}")
    public ResponseEntity<HttpResponse> verifyAccount(@PathVariable("key") String key) {
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message(userSvc.verifyAccount(key).isEnabled() ? "Account already verified" : "Account verified")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/refresh/token")
    public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest request) {
        if (isHeaderAndTokenValid(request)) {
            String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
            UserDTO user = userSvc.getByUserId(tokenProvider.getSubject(token, request));
            return ResponseEntity.ok()
                    .body(HttpResponse.builder()
                            .timeStamp(now().toString())
                            .data(of("user", user, "access_token", tokenProvider.createAccessToken(getUserPrincipal(user)),
                                    "refresh_token", token))
                            .message("Token Refresh")
                            .status(OK)
                            .statusCode(OK.value())
                            .build());
        }

        return ResponseEntity.badRequest()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("Refresh token missing or invalid.")
                        .developerMessage("Refresh token missing or invalid.")
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build());
    }

    @PatchMapping("/update")
    public ResponseEntity<HttpResponse> updateUser(@RequestBody @Valid UpdateRequest user) throws InterruptedException {
        UserDTO updatedUser = userSvc.updateUserDetails(user);
        publisher.publishEvent(new NewUserEvent(updatedUser.getEmail(), PROFILE_UPDATE));
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user, "roles", roleSvc.getRoles(), "events", eventSvc.getEventsByUserId(user.getId())))
                        .message("Profile updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update/password")
    public ResponseEntity<HttpResponse> updatePassword(Authentication authentication, @RequestBody @Valid UpdatePasswordRequest request) {
        UserDTO user = getAuthenticatedUser(authentication);
        userSvc.updatePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword(), request.getConfirmNewPassword());
        publisher.publishEvent(new NewUserEvent(user.getEmail(), PASSWORD_UPDATE));
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getByUserId(user.getId()), "roles", roleSvc.getRoles(), "events", eventSvc.getEventsByUserId(user.getId())))
                        .message("Password updated successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update/role/{roleName}")
    public ResponseEntity<HttpResponse> updateRole(Authentication authentication, @PathVariable("roleName") String roleName) {
        UserDTO user = getAuthenticatedUser(authentication);
        userSvc.updateUserRole(user.getId(), roleName);
        publisher.publishEvent(new NewUserEvent(user.getEmail(), ROLE_UPDATE));
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getByUserId(user.getId()), "roles", roleSvc.getRoles(), "events", eventSvc.getEventsByUserId(user.getId())))
                        .message("Role updated successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update/settings")
    public ResponseEntity<HttpResponse> updateAccountSettings(Authentication authentication, @RequestBody @Valid SettingsRequest request) {
        UserDTO user = getAuthenticatedUser(authentication);
        userSvc.updateAccountSettings(user.getId(), request.getEnabled(), request.getNotLocked());
        publisher.publishEvent(new NewUserEvent(user.getEmail(), ACCOUNT_SETTINGS_UPDATE));
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getByUserId(user.getId()), "roles", roleSvc.getRoles(), "events", eventSvc.getEventsByUserId(user.getId())))
                        .message("Account settings updated successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/togglemfa")
    public ResponseEntity<HttpResponse> toggleMfa(Authentication authentication) throws InterruptedException {
        UserDTO user = userSvc.toggleMfa(getAuthenticatedUser(authentication).getEmail());
        publisher.publishEvent(new NewUserEvent(user.getEmail(), MFA_UPDATE));
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getByUserId(user.getId()), "roles", roleSvc.getRoles(), "events", eventSvc.getEventsByUserId(user.getId())))
                        .message("Multi-Factor authentication updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update/image")
    public ResponseEntity<HttpResponse> updateProfileImage(Authentication authentication, @RequestParam("image") MultipartFile image) {
        UserDTO user = getAuthenticatedUser(authentication);
        userSvc.updateImage(user, image);
        publisher.publishEvent(new NewUserEvent(user.getEmail(), PROFILE_PICTURE_UPDATE));
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", userSvc.getByUserId(user.getId()), "roles", roleSvc.getRoles(), "events", eventSvc.getEventsByUserId(user.getId())))
                        .message("Profile image updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping(value = "/image/{fileName}", produces = IMAGE_PNG_VALUE)
    public byte[] getProfileImage(@PathVariable("fileName") String fileName) throws Exception {
        return Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/Downloads/image/" + fileName));
    }

    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> handleError(HttpServletRequest request) {
        return new ResponseEntity<>(HttpResponse.builder()
                .timeStamp(now().toString())
                .reason("There is no mapping for a " + request.getMethod() + " request path on the server.")
                .status(NOT_FOUND)
                .statusCode(NOT_FOUND.value())
                .build(), NOT_FOUND);
    }

    private boolean isHeaderAndTokenValid(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION) != null &&
                request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX) &&
                tokenProvider.isTokenValid(tokenProvider.getSubject(request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length()), request),
                        request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length()));
    }

    private UserDTO authenticate(String email, String password) {
        try {
            if (userSvc.getUserByEmail(email) != null) {
                publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT));
            }
            Authentication auth = authManager.authenticate(unauthenticated(email, password));
            UserDTO loggedInUser = getLoggedInUser(auth);

            if (!loggedInUser.isUsingMfa()) {
                publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT_SUCCESS));
            }
            return loggedInUser;
        } catch (Exception ex) {
            publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT_FAILURE));
            processError(request, response, ex);
            throw new ApiException(ex.getMessage());
        }
    }

    private ResponseEntity<HttpResponse> sendResponse(UserDTO user) {
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user, "access_token", tokenProvider.createAccessToken(getUserPrincipal(user)),
                                "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(user))))
                        .message("User logged in successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    private UserPrincipal getUserPrincipal(UserDTO user) {
        return new UserPrincipal(toUser(userSvc.getUserByEmail(user.getEmail())), roleSvc.getRoleByUserId(user.getId()));
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO user) {
        userSvc.sendVerificationCode(user);

        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user))
                        .message("Verification code sent")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    private URI getURI() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/user/get/<userId>").toUriString());
    }
}
