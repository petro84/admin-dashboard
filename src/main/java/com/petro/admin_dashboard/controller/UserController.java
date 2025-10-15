package com.petro.admin_dashboard.controller;

import com.petro.admin_dashboard.exception.ApiException;
import com.petro.admin_dashboard.model.HttpResponse;
import com.petro.admin_dashboard.model.LoginRequest;
import com.petro.admin_dashboard.model.User;
import com.petro.admin_dashboard.model.UserPrincipal;
import com.petro.admin_dashboard.model.dto.UserDTO;
import com.petro.admin_dashboard.provider.TokenProvider;
import com.petro.admin_dashboard.service.RoleService;
import com.petro.admin_dashboard.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static com.petro.admin_dashboard.mapper.UserDTOMapper.toUser;
import static com.petro.admin_dashboard.utils.ExceptionUtils.processError;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.*;
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
        Authentication authentication = authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        UserDTO user = getAuthUser(authentication);
        System.out.println(authentication);
        System.out.println(((UserPrincipal) authentication.getPrincipal()).getUser());
        return user.isUsingMfa() ? sendVerificationCode(user) : sendResponse(user);
    }

    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code) {
       UserDTO user = userSvc.verifyCode(email, code);
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
        UserDTO user = userSvc.getUserByEmail(authentication.getName());
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user))
                        .message("Profile Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> handleError(HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("Request not found.")
                        .status(NOT_FOUND)
                        .statusCode(NOT_FOUND.value())
                        .build());
    }

    private UserDTO getAuthUser(Authentication auth) {
        return ((UserPrincipal) auth.getPrincipal()).getUser();
    }

    private Authentication authenticate(String email, String password) {
        try {
            return authManager.authenticate(unauthenticated(email, password));
        } catch (Exception ex) {
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
